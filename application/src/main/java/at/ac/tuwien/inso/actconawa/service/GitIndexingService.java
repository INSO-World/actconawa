package at.ac.tuwien.inso.actconawa.service;

import at.ac.tuwien.inso.actconawa.events.CommitIndexingDoneEvent;
import at.ac.tuwien.inso.actconawa.persistence.GitBranch;
import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitRelationship;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitRelationshipKey;
import at.ac.tuwien.inso.actconawa.repository.GitBranchRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRelationshipRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GitIndexingService {


    private static final Logger LOG = LoggerFactory.getLogger(GitIndexingService.class);

    private final GitBranchRepository gitBranchRepository;

    private final GitCommitRepository gitCommitRepository;

    private final GitCommitRelationshipRepository gitCommitRelationshipRepository;

    private final Git git;

    private final Repository repository;

    private final ApplicationEventPublisher applicationEventPublisher;


    public GitIndexingService(
            GitBranchRepository gitBranchRepository,
            GitCommitRepository gitCommitRepository,
            GitCommitRelationshipRepository gitCommitRelationshipRepository,
            Git git,
            ApplicationEventPublisher applicationEventPublisher) {
        this.gitBranchRepository = gitBranchRepository;
        this.gitCommitRepository = gitCommitRepository;
        this.gitCommitRelationshipRepository = gitCommitRelationshipRepository;
        this.git = git;
        this.repository = git.getRepository();
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void index() throws IOException, GitAPIException {
        var commitCache = new HashMap<String, GitCommit>();
        var startTimestamp = Instant.now();
        LOG.info("Starting indexing of {}", repository.getDirectory());

        LOG.info("Start indexing branches");
        final var allRemoteBranches = git.branchList()
                .setListMode(ListBranchCommand.ListMode.REMOTE)
                .call();
        final var remoteHeads = allRemoteBranches.stream()
                .filter(x -> x.isSymbolic() && StringUtils.endsWith(x.getName(), "/HEAD"))
                .map(x -> x.getTarget().getName())
                .collect(Collectors.toSet());

        final var branches = new ArrayList<GitBranch>();
        final var branchMap = new HashMap<String, GitBranch>();
        for (Ref remoteBranch : allRemoteBranches) {
            if (remoteBranch.isSymbolic()) {
                continue;
            }
            var gitBranch = new GitBranch();
            gitBranch.setRemoteHead(remoteHeads.contains(remoteBranch.getName()));
            gitBranch.setName(StringUtils.removeStart(remoteBranch.getName(), "refs/remotes/"));
            branches.add(gitBranch);
            branchMap.put(remoteBranch.getName(), gitBranch);

            indexCommits(remoteBranch, gitBranch, commitCache);
        }
        gitBranchRepository.saveAll(branches);
        for (var commitCacheEntry : commitCache.entrySet()) {
            var branchesOfCommit = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.REMOTE)
                    .setContains(commitCacheEntry.getKey()).call()
                    .stream()
                    .map(x -> x.getObjectId().getName())
                    .map(branchMap::get)
                    .toList();
            if (commitCacheEntry.getValue().getBranches() == null) {
                commitCacheEntry.getValue().setBranches(new ArrayList<>());
            }
            commitCacheEntry.getValue().getBranches().addAll(branchesOfCommit);
        }

        LOG.info("Done indexing {} branches in {}",
                branches.size(),
                Duration.between(startTimestamp, Instant.now()));
        applicationEventPublisher.publishEvent(new CommitIndexingDoneEvent(this));
    }

    private void indexCommits(
            Ref remoteBranchRef,
            GitBranch gitBranch,
            HashMap<String, GitCommit> commitCache)
            throws IOException {
        var revWalk = new RevWalk(repository);
        revWalk.sort(RevSort.TOPO);
        revWalk.markStart(revWalk.parseCommit(remoteBranchRef.getObjectId()));
        LOG.debug("Start indexing commits of branch {}", gitBranch.getName());
        // first commit of revwalk is always the head commit of the branch,
        // so no string sha comparisons are necessary
        boolean headCommitProcessed = false;
        var indexedCommitCount = 0;
        var relationships = new ArrayList<GitCommitRelationship>();
        for (var commit : revWalk) {
            if (commitCache.containsKey(commit.getId().getName())) {
                var cachedCommit = commitCache.get(commit.getId().getName());
                if (cachedCommit.getId() != null) {
                    if (!headCommitProcessed) {
                        gitBranch.setHeadCommit(cachedCommit);
                        headCommitProcessed = true;
                    }
                    continue;
                }
            }

            commitCache.putIfAbsent(commit.getId().getName(), new GitCommit());
            final GitCommit gitCommit = commitCache.get(commit.getId().getName());
            gitCommit.setSha(commit.getName());
            gitCommit.setMessage(commit.getShortMessage()
                    .substring(0,
                            Math.min(commit.getShortMessage().length(), 255)));
            gitCommit.setAuthorName(commit.getAuthorIdent().getName());
            gitCommit.setAuthorEmail(commit.getAuthorIdent().getEmailAddress());
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=319142
            gitCommit.setCommitDate(LocalDateTime.ofEpochSecond(commit.getCommitTime(),
                    0,
                    ZoneOffset.UTC));
            if (!headCommitProcessed) {
                gitBranch.setHeadCommit(gitCommit);
                headCommitProcessed = true;
            }
            Optional.ofNullable(commit.getParents()).stream()
                    .flatMap(Arrays::stream)
                    .forEach(parent -> {
                        commitCache.putIfAbsent(parent.getId().getName(), new GitCommit());
                        var parentCommit = commitCache.get(parent.getId().getName());
                        var relationship = new GitCommitRelationship();
                        var relationshipKey = new GitCommitRelationshipKey();
                        relationship.setChild(gitCommit);
                        relationship.setParent(parentCommit);
                        relationship.setId(relationshipKey);
                        relationships.add(relationship);
                    });
            gitCommit.setId(gitCommitRepository.save(gitCommit).getId());
            indexedCommitCount++;
        }
        relationships.forEach(x -> {
            x.getId().setChild(x.getChild().getId());
            x.getId().setParent(x.getParent().getId());
        });
        gitCommitRelationshipRepository.saveAllAndFlush(relationships);
        gitBranch.setContainingExclusiveCommits(indexedCommitCount != 0);
        LOG.debug("Done indexing {} unique commits of branch {}",
                indexedCommitCount,
                gitBranch.getName());
    }

}
