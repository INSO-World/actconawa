package at.ac.tuwien.inso.actconawa.index;

import at.ac.tuwien.inso.actconawa.enums.MergeStatus;
import at.ac.tuwien.inso.actconawa.exception.CommitNotFoundException;
import at.ac.tuwien.inso.actconawa.exception.IndexingIOException;
import at.ac.tuwien.inso.actconawa.persistence.GitBranch;
import at.ac.tuwien.inso.actconawa.persistence.GitBranchTrackingStatus;
import at.ac.tuwien.inso.actconawa.repository.GitBranchRepository;
import at.ac.tuwien.inso.actconawa.repository.GitBranchTrackingStatusRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRepository;
import at.ac.tuwien.inso.actconawa.service.GitCommitService;
import jakarta.transaction.Transactional;
import org.eclipse.jgit.errors.NoMergeBaseException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.eclipse.jgit.merge.MergeStrategy.SIMPLE_TWO_WAY_IN_CORE;

@Component
@Order(2)
/*
 * Index maximum distance to root commit and lowest common ancestors of branch heads.
 */
public class GitBranchTrackingStatusIndexer implements Indexer {

    private static final Logger LOG = LoggerFactory.getLogger(GitBranchTrackingStatusIndexer.class);

    private final GitBranchRepository gitBranchRepository;

    private final GitCommitRepository gitCommitRepository;

    private final GitBranchTrackingStatusRepository gitBranchTrackingStatusRepository;

    private final GitCommitService gitCommitService;


    private final Repository repository;

    ;

    public GitBranchTrackingStatusIndexer(
            GitBranchRepository gitBranchRepository,
            GitCommitRepository gitCommitRepository,
            GitBranchTrackingStatusRepository gitBranchTrackingStatusRepository,
            GitCommitService gitCommitService,
            Repository repository) {
        this.gitBranchRepository = gitBranchRepository;
        this.gitCommitRepository = gitCommitRepository;
        this.gitBranchTrackingStatusRepository = gitBranchTrackingStatusRepository;
        this.gitCommitService = gitCommitService;
        this.repository = repository;
    }


    @Override
    @Transactional
    public void index() {
        var branches = gitBranchRepository.findAll();
        for (GitBranch branchA : branches) {
            for (GitBranch branchB : branches) {
                if (branchA == branchB) {
                    continue;
                }
                try (RevWalk walk = new RevWalk(repository)) {
                    // find merge base
                    walk.setRevFilter(RevFilter.MERGE_BASE);
                    var refA = walk.parseCommit(ObjectId.fromString(branchA.getHeadCommit().getSha()));
                    var refB = walk.parseCommit(ObjectId.fromString(branchB.getHeadCommit().getSha()));
                    walk.markStart(refA);
                    walk.markStart(refB);
                    RevCommit mergeBase = walk.next();
                    var gitCommitMergeBase = gitCommitRepository.findByShaStartsWith(mergeBase.getName())
                            .orElseThrow(CommitNotFoundException::new);

                    LOG.debug("Merge Base of {} and {} is commit {}",
                            branchA.getName(), branchB.getName(), mergeBase.getName());
                    // reset walk and get difference count
                    walk.reset();
                    walk.setRevFilter(RevFilter.ALL);
                    int aheadCount = RevWalkUtils.count(walk, refA, mergeBase);
                    int behindCount = RevWalkUtils.count(walk, refB, mergeBase);
                    LOG.debug("Branch {} and {} is {} ahead and {} behind",
                            branchA.getName(), branchB.getName(), aheadCount, behindCount);
                    // check if a branch b was merged into a already
                    var isMergedInto = walk.isMergedInto(refB, refA);
                    var mergeStatus = MergeStatus.CONFLICTS;
                    if (!isMergedInto) {
                        try {
                            var mergeable = SIMPLE_TWO_WAY_IN_CORE.newMerger(repository, true).merge(refA, refB);
                            mergeStatus = isMergedInto ? MergeStatus.MERGED :
                                    mergeable ? MergeStatus.MERGEABLE : MergeStatus.CONFLICTS;

                        } catch (NoMergeBaseException e) {
                            mergeStatus = MergeStatus.UNKNOWN_MERGE_BASE;
                            LOG.error("Branch {} and {} has no merge-base or multiple", e);
                        }
                    }
                    // persist
                    var branchTrackingStatus = new GitBranchTrackingStatus();
                    branchTrackingStatus.setBranchA(branchA);
                    branchTrackingStatus.setBranchB(branchB);
                    branchTrackingStatus.setMergeStatus(mergeStatus);
                    branchTrackingStatus.setAheadCount(aheadCount);
                    branchTrackingStatus.setBehindCount(behindCount);
                    branchTrackingStatus.setMergeBase(gitCommitMergeBase);
                    gitBranchTrackingStatusRepository.save(branchTrackingStatus);
                } catch (IOException e) {
                    throw new IndexingIOException(e);
                }
            }
        }


    }

    @Override
    public String getIndexedContentDescription() {
        return "lowest common ancestors of branch heads and distance to root commit";
    }


}
