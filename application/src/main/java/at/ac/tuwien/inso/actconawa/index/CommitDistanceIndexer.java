package at.ac.tuwien.inso.actconawa.index;

import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

@Component
@Order(2)
/**
 * Index maximum distance to root commit. required to return the ancestry tree of a commit in order.
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
        writeCommitRootDistance(rootCommits.get(0));

    }

    @Override
    public String getIndexedContentDescription() {
        return "distance to root commit";
    }

    private void writeCommitRootDistance(GitCommit rootCommit) {
        if (rootCommit == null) {
            return;
        }
        Queue<Pair<GitCommit, Integer>> queue = new LinkedList<>();

        queue.add(Pair.of(rootCommit, 0));

        while (!queue.isEmpty()) {
            Pair<GitCommit, Integer> current = queue.poll();
            Integer currentDistance = current.getSecond();
            GitCommit currentCommit = current.getFirst();

            if (currentCommit.getMaxDistanceFromRoot() == null
                    || currentCommit.getMaxDistanceFromRoot() < currentDistance) {
                currentCommit.setMaxDistanceFromRoot(currentDistance);
            }
            currentCommit.getChildren().forEach(x -> queue.add(Pair.of(x.getChild(), currentDistance + 1)));
        }

    }


}
