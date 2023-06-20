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
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

            LOG.info("Starting indexing of {}", repo);

            // Branches
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
                var revWalk = new RevWalk(repository);
                revWalk.markStart(revWalk.parseCommit(remoteBranch.getObjectId()));
                indexCommits(revWalk, gitBranch, commitCache);
            }
            gitBranchRepository.saveAll(branches);
            LOG.info("Done indexing {} branches", branches.size());

            // CommitRelations
            LOG.info("Start indexing of parent-child relationship of the commits");
            var relationShipCounter = 0;
            for (RevCommit commit : git.log().all().call()) {
                if (!commitCache.containsKey(commit.getId().getName())) {
                    continue;
                }
                relationShipCounter += Arrays.stream(commit.getParents()).mapToInt(x -> {
                            var parent = commitCache.get(x.getId().getName());
                            if (parent == null) {
                                throw new IllegalArgumentException("No such parent commit existing");
                            }
                            var child = commitCache.get(commit.getName());
                            if (child == null) {
                                throw new IllegalArgumentException("No such child commit existing");
                            }
                            var relationship = new GitCommitRelationship();
                            var relationshipKey = new GitCommitRelationshipKey();
                            relationshipKey.setChild(child.getId());
                            relationshipKey.setParent(parent.getId());
                            relationship.setId(relationshipKey);
                            gitCommitRelationshipRepository.save(relationship);
                            return 1;
                        }
                ).sum();
            }
            LOG.info("Done indexing {} commits relationships", relationShipCounter);
        } catch (GitAPIException e) {
            // TODO: Exception Handling
        }

    }

    private void indexCommits(RevWalk revWalk, GitBranch gitBranch, HashMap<String, GitCommit> commitCache) {
        LOG.info("Start indexing commits of branch {}", gitBranch.getName());
        // first commit of revwalk is always the head commit of the branch,
        // so no string sha comparisons are necessary
        boolean headCommitProcessed = false;
        int indexedCommitCount = 0;
        for (var commit : revWalk) {
            if (commitCache.containsKey(commit.getId().getName())) {
                var cachedCommit = commitCache.get(commit.getId().getName());
                cachedCommit.getBranches().add(gitBranch);
                if (!headCommitProcessed) {
                    gitBranch.setHeadCommit(cachedCommit);
                    headCommitProcessed = true;
                }
                continue;
            }
            final GitCommit gitCommit = new GitCommit();
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
            final var savedGitCommit = gitCommitRepository.save(gitCommit);
            indexedCommitCount++;
            commitCache.put(commit.getName(), savedGitCommit);
        }
        gitBranch.setContainingExclusiveCommits(indexedCommitCount != 0);
        LOG.info("Done indexing {} unique commits of branch {}",
                indexedCommitCount,
                gitBranch.getName());
    }

}
