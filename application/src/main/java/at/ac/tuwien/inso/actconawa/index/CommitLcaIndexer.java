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

import java.util.Comparator;
import java.util.HashSet;
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
        var branchHeads = gitCommitRepository.findBranchHeadCommits();
        Map<GitCommit, Set<GitCommit>> headsMultiChildParents = branchHeads.stream()
                .collect(Collectors.toMap(x -> x, x -> new HashSet<>()));
        Map<GitCommit, Set<GitCommit>> headsAncestorHeads = branchHeads.stream()
                .collect(Collectors.toMap(x -> x, x -> new HashSet<>()));

        traverseTreeWriteDistanceAndCollectPotentialLCACandidates(
                0,
                rootCommits.get(0),
                headsMultiChildParents,
                new HashSet<>(),
                new HashSet<>(),
                headsAncestorHeads);

        Set<GitCommitLowestCommonAncestor> lowestCommonAncestors = new HashSet<>();
        for (GitCommit commitA : branchHeads) {
            for (GitCommit commitB : branchHeads) {
                // the two commits are the same
                if (commitA == commitB) {
                    continue;
                }
                // commit b is ancestor of commit a == lca
                if (headsAncestorHeads.get(commitA).contains(commitB)) {
                    var entity = new GitCommitLowestCommonAncestor();
                    entity.setCommitA(commitA);
                    entity.setCommitB(commitB);
                    entity.setLowestCommonAncestorCommit(commitB);
                    lowestCommonAncestors.add(entity);
                    continue;
                }
                // commit a is ancestor of commit b == lca
                if (headsAncestorHeads.get(commitB).contains(commitA)) {
                    var entity = new GitCommitLowestCommonAncestor();
                    entity.setCommitA(commitA);
                    entity.setCommitB(commitB);
                    entity.setLowestCommonAncestorCommit(commitA);
                    lowestCommonAncestors.add(entity);
                    continue;
                }
                // lca is a multi child commit
                var potentialLcaA = headsMultiChildParents.get(commitA);
                var potentialLcaB = headsMultiChildParents.get(commitB);
                potentialLcaA.stream()
                        .filter(potentialLcaB::contains)
                        .max(Comparator.comparingInt(GitCommit::getMaxDistanceFromRoot))
                        .map(lca -> {
                            var entity = new GitCommitLowestCommonAncestor();
                            entity.setCommitA(commitA);
                            entity.setCommitB(commitB);
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

    /**
     * Traverse the git tree of a certain commit recursively and write the max. distance to the root into each commit.
     *
     * Further fills the {@link Map} containing branch heads as keys with all corresponding multi child ancestors.
     *
     * @param distance               the current distance to the root commit
     * @param commit                 the commit that is currently processed (to be called initially usually with root
     *                               commit)
     * @param headsMultiChildParents the {@link Map} containing the the head commits that are under investigation as
     *                               keys and all multi child ancestor commits as values.
     * @param multiChildCommits      all the multi-child commits that were processed so far on the path to a commit
     * @param branchHeads            all the branch head commits that were processed so far on the path to a commit
     * @param headsAncestorHeads     the {@link Map} containing the the head commits that are under investigation as
     *                               keys and all ancestor head commits as values.
     */
    private void traverseTreeWriteDistanceAndCollectPotentialLCACandidates(
            int distance, GitCommit commit,
            Map<GitCommit, Set<GitCommit>> headsMultiChildParents,
            Set<GitCommit> multiChildCommits,
            Set<GitCommit> branchHeads,
            Map<GitCommit, Set<GitCommit>> headsAncestorHeads
    ) {
        // Update distance is commit distance from root is still null or smaller than current distance value
        if (commit.getMaxDistanceFromRoot() == null || commit.getMaxDistanceFromRoot() < distance) {
            commit.setMaxDistanceFromRoot(distance);
        }

        // If it is a commit of interest (branch head). Add all potential LCA candidates.
        if (headsMultiChildParents.containsKey(commit)) {
            // set ensures no duplicates that would occur here
            headsMultiChildParents.get(commit).addAll(multiChildCommits);
        }
        if (headsAncestorHeads.containsKey(commit)) {
            // set ensures no duplicates that would occur here
            headsAncestorHeads.get(commit).addAll(branchHeads);
        }
        // Create shallow copy to not influence recursion
        var multiChildCommitsCopy = new HashSet<>(multiChildCommits);
        var branchHeadsCopy = new HashSet<>(branchHeads);
        // add commit to the freshly copied list of multi child commits if this commit has multiple children
        if (commit.getChildren() != null && commit.getChildren().size() > 1) {
            multiChildCommitsCopy.add(commit);
        }
        // add commit to the branchheads copy if the commit is a branch head
        if (headsAncestorHeads.containsKey(commit)) {
            branchHeadsCopy.add(commit);
        }
        var nextDistance = ++distance;
        // for all the children in the commit, repeat this.
        for (GitCommitRelationship x : commit.getChildren()) {
            traverseTreeWriteDistanceAndCollectPotentialLCACandidates(
                    nextDistance,
                    x.getChild(),
                    headsMultiChildParents,
                    multiChildCommitsCopy,
                    branchHeadsCopy,
                    headsAncestorHeads
            );
        }

    }
}
