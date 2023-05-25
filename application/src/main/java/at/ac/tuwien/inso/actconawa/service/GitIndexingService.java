package at.ac.tuwien.inso.actconawa.service;

import at.ac.tuwien.inso.actconawa.persistence.GitBranch;
import at.ac.tuwien.inso.actconawa.persistence.GitBranchCommit;
import at.ac.tuwien.inso.actconawa.persistence.GitBranchCommitKey;
import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitRelationship;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitRelationshipKey;
import at.ac.tuwien.inso.actconawa.repository.GitBranchCommitRepository;
import at.ac.tuwien.inso.actconawa.repository.GitBranchRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRelationshipRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.revwalk.RevCommit;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitIndexingService {


    private static final Logger LOG = LoggerFactory.getLogger(GitIndexingService.class);


    private final GitBranchRepository gitBranchRepository;

    private final GitCommitRepository gitCommitRepository;

    private final GitCommitRelationshipRepository gitCommitRelationshipRepository;

    private final GitBranchCommitRepository gitBranchCommitRepository;

    @Value("${actconawa.repo}")
    private String repo;

    public GitIndexingService(
            GitBranchRepository gitBranchRepository,
            GitCommitRepository gitCommitRepository,
            GitCommitRelationshipRepository gitCommitRelationshipRepository,
            GitBranchCommitRepository gitBranchCommitRepository) {
        this.gitBranchRepository = gitBranchRepository;
        this.gitCommitRepository = gitCommitRepository;
        this.gitCommitRelationshipRepository = gitCommitRelationshipRepository;
        this.gitBranchCommitRepository = gitBranchCommitRepository;
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
            var objectIdToBranchCache = new HashMap<String, List<GitBranch>>();
            var nameToBranchCache = new HashMap<String, GitBranch>();
            final var allRemoteBranches = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.REMOTE)
                    .call();
            final var remoteHeads = allRemoteBranches.stream()
                    .filter(x -> x.isSymbolic() && StringUtils.endsWith(x.getName(), "/HEAD"))
                    .map(x -> x.getTarget().getName())
                    .collect(Collectors.toSet());
            final var branches = allRemoteBranches
                    .stream()
                    .filter(x -> !x.isSymbolic())
                    .map(x -> {
                        var gitBranch = new GitBranch();
                        objectIdToBranchCache.putIfAbsent(x.getObjectId().getName(),
                                new ArrayList<>());
                        objectIdToBranchCache.get(x.getObjectId().getName()).add(gitBranch);
                        gitBranch.setRemoteHead(remoteHeads.contains(x.getName()));
                        gitBranch.setName(StringUtils.removeStart(x.getName(), "refs/remotes/"));
                        nameToBranchCache.put(x.getName(), gitBranch);
                        return gitBranch;
                    })
                    .collect(Collectors.toList());
            gitBranchRepository.saveAll(branches);
            LOG.info("Done indexing {} branches", branches.size());

            LOG.info("Starting indexing of commits");
            // Commits
            for (RevCommit commit : git.log().all().call()) {
                final GitCommit gitCommit = new GitCommit();
                gitCommit.setSha(commit.getName());
                gitCommit.setMessage(commit.getShortMessage()
                        .substring(0, Math.min(commit.getShortMessage().length(), 255)));
                gitCommit.setAuthorName(commit.getAuthorIdent().getName());
                gitCommit.setAuthorEmail(commit.getAuthorIdent().getEmailAddress());
                // https://bugs.eclipse.org/bugs/show_bug.cgi?id=319142
                gitCommit.setCommitDate(LocalDateTime.ofEpochSecond(commit.getCommitTime(),
                        0,
                        ZoneOffset.UTC));
                final var savedGitCommit = gitCommitRepository.save(gitCommit);
                objectIdToBranchCache.computeIfPresent(commit.getName(), (ref, cachedBranches) -> {
                            cachedBranches.forEach(branch -> {
                                branch.setHeadCommit(savedGitCommit);
                            });
                            return cachedBranches;
                        }
                );
                commitCache.put(commit.getName(), savedGitCommit);
            }
            LOG.info("Done indexing {} commits", commitCache.size());

            // Branches of Commits
            LOG.info("Starting indexing of commit-branch relationships");
            var commitBranches = new ArrayList<GitBranchCommit>();
            for (GitCommit gitCommit : commitCache.values()) {
                git.branchList()
                        .setListMode(ListBranchCommand.ListMode.REMOTE)
                        .setContains(gitCommit.getSha())
                        .call()
                        .stream().filter(x -> !x.isSymbolic())
                        .forEach(branchWithCommit -> {
                            var gitBranchCommit = new GitBranchCommit();
                            var gitBranchCommitKey = new GitBranchCommitKey();
                            gitBranchCommitKey.setBranch(nameToBranchCache.get(branchWithCommit.getName())
                                    .getId());
                            gitBranchCommitKey.setCommit(gitCommit.getId());
                            gitBranchCommit.setId(gitBranchCommitKey);
                            commitBranches.add(gitBranchCommit);
                        });
            }
            gitBranchCommitRepository.saveAll(commitBranches);
            LOG.info("Done indexing of {} commit-branch relationships", commitBranches.size());

            // CommitRelations
            LOG.info("Start indexing of parent-child relationship of the commits");
            var relationShipCounter = 0;
            for (RevCommit commit : git.log().all().call()) {
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

}
