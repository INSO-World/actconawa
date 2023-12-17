package at.ac.tuwien.inso.actconawa.index;

import at.ac.tuwien.inso.actconawa.exception.IndexingGitApiException;
import at.ac.tuwien.inso.actconawa.exception.IndexingIOException;
import at.ac.tuwien.inso.actconawa.persistence.GitBranch;
import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitRelationship;
import at.ac.tuwien.inso.actconawa.repository.GitBranchRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRelationshipRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

@Component
@Order(1)
public class GitCommitIndexer implements Indexer {


    private static final Logger LOG = LoggerFactory.getLogger(GitCommitIndexer.class);

    private final GitBranchRepository gitBranchRepository;

    private final GitCommitRepository gitCommitRepository;

    private final GitCommitRelationshipRepository gitCommitRelationshipRepository;

    private final Git git;

    private final Repository repository;

    public GitCommitIndexer(
            GitBranchRepository gitBranchRepository,
            GitCommitRepository gitCommitRepository,
            GitCommitRelationshipRepository gitCommitRelationshipRepository,
            Git git) {
        this.gitBranchRepository = gitBranchRepository;
        this.gitCommitRepository = gitCommitRepository;
        this.gitCommitRelationshipRepository = gitCommitRelationshipRepository;
        this.git = git;
        this.repository = git.getRepository();
    }

    @Transactional
    public void index() {
        try {
            indexInternal();
        } catch (GitAPIException e) {
            throw new IndexingGitApiException(e);
        } catch (IOException e) {
            throw new IndexingIOException(e);
        }
    }

    @Override
    public String getIndexedContentDescription() {
        return "commit and branch information";
    }

    private void indexInternal() throws GitAPIException, IOException {
        var commitCache = new HashMap<String, GitCommit>();
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
                    .map(Ref::getName)
                    .map(branchMap::get)
                    .toList();
            if (commitCacheEntry.getValue().getBranches() == null) {
                commitCacheEntry.getValue().setBranches(new ArrayList<>());
            }
            commitCacheEntry.getValue().getBranches().addAll(branchesOfCommit);
        }
    }

    private void indexCommits(
            Ref remoteBranchRef,
            GitBranch gitBranch,
            HashMap<String, GitCommit> commitCache) throws IOException {
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
            var parents = commit.getParents();
            if (ArrayUtils.isNotEmpty(parents)) {
                for (RevCommit parent : parents) {
                    commitCache.putIfAbsent(parent.getId().getName(), new GitCommit());
                    var parentCommit = commitCache.get(parent.getId().getName());
                    var relationship = new GitCommitRelationship();
                    relationship.setChild(gitCommit);
                    relationship.setParent(parentCommit);
                    relationships.add(relationship);
                }
            } else {
                var relationship = new GitCommitRelationship();
                relationship.setChild(gitCommit);
                relationships.add(relationship);
            }

            gitCommit.setId(gitCommitRepository.save(gitCommit).getId());
            indexedCommitCount++;
        }
        relationships.forEach(x -> {
            x.setChild(x.getChild());
            x.setParent(x.getParent() == null ? null : x.getParent());
        });
        gitCommitRelationshipRepository.saveAllAndFlush(relationships);
        gitBranch.setContainingExclusiveCommits(indexedCommitCount != 0);
        LOG.debug("Done indexing {} unique commits of branch {}",
                indexedCommitCount,
                gitBranch.getName());
    }

}
