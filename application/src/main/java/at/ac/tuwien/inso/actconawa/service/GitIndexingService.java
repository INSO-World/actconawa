package at.ac.tuwien.inso.actconawa.service;

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
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
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


    @Value("${actconawa.repo}")
    private String repo;

    public GitIndexingService(
            GitBranchRepository gitBranchRepository,
            GitCommitRepository gitCommitRepository,
            GitCommitRelationshipRepository gitCommitRelationshipRepository) {
        this.gitBranchRepository = gitBranchRepository;
        this.gitCommitRepository = gitCommitRepository;
        this.gitCommitRelationshipRepository = gitCommitRelationshipRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void index() throws IOException {
        var repository = new FileRepository(repo);
        try (Git git = new Git(repository)) {
            var commitCache = new HashMap<String, GitCommit>();
            var startTimestamp = Instant.now();
            LOG.info("Starting indexing of {}", repo);

            LOG.info("Start indexing branches");
            final var allRemoteBranches = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.REMOTE)
                    .call();
            final var remoteHeads = allRemoteBranches.stream()
                    .filter(x -> x.isSymbolic() && StringUtils.endsWith(x.getName(), "/HEAD"))
                    .map(x -> x.getTarget().getName())
                    .collect(Collectors.toSet());

            final var branches = new ArrayList<GitBranch>();
            for (Ref remoteBranch : allRemoteBranches) {
                if (remoteBranch.isSymbolic()) {
                    continue;
                }
                var gitBranch = new GitBranch();
                gitBranch.setRemoteHead(remoteHeads.contains(remoteBranch.getName()));
                gitBranch.setName(StringUtils.removeStart(remoteBranch.getName(), "refs/remotes/"));
                branches.add(gitBranch);

                indexCommits(repository, remoteBranch, gitBranch, commitCache);
            }
            gitBranchRepository.saveAll(branches);
            LOG.info("Done indexing {} branches in {}",
                    branches.size(),
                    Duration.between(startTimestamp, Instant.now()));
        } catch (GitAPIException e) {
            // TODO: Exception Handling
        }

    }

    private void indexCommits(Repository repository,
            Ref remoteBranchRef,
            GitBranch gitBranch,
            HashMap<String, GitCommit> commitCache)
            throws IOException {
        var revWalk = new RevWalk(repository);
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
                    cachedCommit.getBranches().add(gitBranch);
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
            if (gitCommit.getBranches() == null) {
                gitCommit.setBranches(new ArrayList<>());
            }
            gitCommit.getBranches().add(gitBranch);
            if (!headCommitProcessed) {
                gitBranch.setHeadCommit(gitCommit);
                headCommitProcessed = true;
            }
            Optional.ofNullable(commit.getParents()).stream()
                    .flatMap(Arrays::stream)
                    .forEach(x -> {
                        commitCache.putIfAbsent(x.getId().getName(), new GitCommit());
                        var parentCommit = commitCache.get(x.getId().getName());
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
        gitCommitRelationshipRepository.saveAll(relationships);
        gitBranch.setContainingExclusiveCommits(indexedCommitCount != 0);
        LOG.debug("Done indexing {} unique commits of branch {}",
                indexedCommitCount,
                gitBranch.getName());
    }

}
