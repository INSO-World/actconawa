package at.ac.tuwien.inso.actconawa.index;

import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitLowestCommonAncestor;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitRelationship;
import at.ac.tuwien.inso.actconawa.repository.GitCommitLowestCommonAncestorRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Order(2)
/*
 * Index maximum distance to root commit and lowest common ancestors of branch heads.
 */
public class CommitLcaIndexer implements Indexer {

    private static final Logger LOG = LoggerFactory.getLogger(CommitLcaIndexer.class);

    private final GitCommitRepository gitCommitRepository;

    private final GitCommitLowestCommonAncestorRepository gitCommitLowestCommonAncestorRepository;

    public CommitLcaIndexer(
            GitCommitRepository gitCommitRepository,
            GitCommitLowestCommonAncestorRepository gitCommitLowestCommonAncestorRepository) {
        this.gitCommitRepository = gitCommitRepository;
        this.gitCommitLowestCommonAncestorRepository = gitCommitLowestCommonAncestorRepository;
    }

    @Override
    @Transactional
    public void index() {
        var rootCommits = gitCommitRepository.findCommitsWithoutParents();
        if (rootCommits.size() != 1) {
            LOG.error("Multi-Root Commit and Empty Repositories are not supported yet");
            // TODO Support multiple root commits (detached ones?)? Check if there are common cases.

        }

        // Set in the value instead of list ensures no duplicate commits
        Map<GitCommit, Set<GitCommit>> headsMultiChildParents = gitCommitRepository
                .findBranchHeadCommits().stream()
                .collect(Collectors.toMap(x -> x, x -> new HashSet<>()));
        writeCommitRootDistance(0, rootCommits.get(0), headsMultiChildParents, new ArrayList<>());

        Set<GitCommitLowestCommonAncestor> lowestCommonAncestors = new HashSet<>();
        for (Map.Entry<GitCommit, Set<GitCommit>> commitA : headsMultiChildParents.entrySet()) {
            for (Map.Entry<GitCommit, Set<GitCommit>> commitB : headsMultiChildParents.entrySet()) {
                if (commitA == commitB) {
                    continue;
                }
                var potentialLcaA = commitA.getValue();
                var potentialLcaB = commitB.getValue();
                potentialLcaA.stream()
                        .filter(potentialLcaB::contains)
                        .max(Comparator.comparingInt(GitCommit::getMaxDistanceFromRoot))
                        .map(lca -> {
                            var entity = new GitCommitLowestCommonAncestor();
                            entity.setCommitA(commitA.getKey());
                            entity.setCommitB(commitB.getKey());
                            entity.setLowestCommonAncestorCommit(lca);
                            return entity;
                        })
                        .ifPresent(lowestCommonAncestors::add);
            }
        }
        gitCommitLowestCommonAncestorRepository.saveAll(lowestCommonAncestors);
    }

    @Override
    public String getIndexedContentDescription() {
        return "lowest common ancestors of branch heads and distance to root commit";
    }

    private void writeCommitRootDistance(
            int distance, GitCommit commit,
            Map<GitCommit, Set<GitCommit>> headsMultiChildParents,
            List<GitCommit> multiChildCommits
    ) {
        if (commit.getMaxDistanceFromRoot() != null && commit.getMaxDistanceFromRoot() > distance) {
            // Already done, break recursion
            return;
        }

        if (headsMultiChildParents.containsKey(commit)) {
            // set ensures no duplicates that would occur here
            headsMultiChildParents.get(commit).addAll(multiChildCommits);
        }
        commit.setMaxDistanceFromRoot(distance);
        var next = new ArrayList<>(multiChildCommits);
        if (commit.getChildren() != null && commit.getChildren().size() > 1) {
            next.add(commit);
        }
        for (GitCommitRelationship x : commit.getChildren()) {
            writeCommitRootDistance(
                    ++distance,
                    x.getChild(),
                    headsMultiChildParents,
                    next
            );
        }

    }
}
