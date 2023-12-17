package at.ac.tuwien.inso.actconawa.index;

import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitRelationship;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
/**
 * Index maximum distance to root commit.
 * This is helpful for efficiently traversing paginated through the commit tree.
 */
public class CommitDistanceIndexer implements Indexer {

    private static final Logger LOG = LoggerFactory.getLogger(CommitDistanceIndexer.class);

    private final GitCommitRepository gitCommitRepository;

    public CommitDistanceIndexer(GitCommitRepository gitCommitRepository) {
        this.gitCommitRepository = gitCommitRepository;
    }

    @Override
    @Transactional
    public void index() {
        var rootCommits = gitCommitRepository.findCommitsWithoutParents();
        if (rootCommits.size() != 1) {
            LOG.error("Multi-Root Commit and Empty Repositories are not supported yet");
            // TODO Support multiple root commits (detached ones?)? Check if there are common cases.

        }
        writeCommitRootDistance(0, rootCommits.get(0));

    }

    @Override
    public String getIndexedContentDescription() {
        return "distance to root commit";
    }

    private void writeCommitRootDistance(int distance, GitCommit commit) {
        if (commit.getMaxDistanceFromRoot() != null && commit.getMaxDistanceFromRoot() > distance) {
            // Already done, break recursion
            return;
        }
        commit.setMaxDistanceFromRoot(distance);
        for (GitCommitRelationship x : commit.getChildren()) {
            writeCommitRootDistance(++distance, x.getChild());
        }

    }
}
