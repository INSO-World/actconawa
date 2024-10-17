package at.ac.tuwien.inso.actconawa.index;

import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitGroup;
import at.ac.tuwien.inso.actconawa.repository.GitCommitGroupRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Stack;

@Component
@Order(2)
/**
 * Index maximum distance to root commit. required to return the ancestry tree of a commit in order.
 * Further index the grouping information of commits
 */
public class CommitGraphHelperIndexer implements Indexer {

    private static final Logger LOG = LoggerFactory.getLogger(CommitGraphHelperIndexer.class);

    private final GitCommitRepository gitCommitRepository;

    private final GitCommitGroupRepository gitCommitGroupRepository;

    public CommitGraphHelperIndexer(GitCommitRepository gitCommitRepository,
            GitCommitGroupRepository gitCommitGroupRepository
    ) {
        this.gitCommitRepository = gitCommitRepository;
        this.gitCommitGroupRepository = gitCommitGroupRepository;
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
        return "distance to root commit and grouping infos";
    }

    private void writeCommitRootDistance(GitCommit rootCommit) {
        if (rootCommit == null) {
            return;
        }
        Stack<Pair<GitCommit, Integer>> stack = new Stack<>();
        stack.add(Pair.of(rootCommit, 0));

        GitCommitGroup group = new GitCommitGroup();
        while (!stack.isEmpty()) {
            Pair<GitCommit, Integer> current = stack.pop();
            Integer currentDistance = current.getSecond();
            GitCommit currentCommit = current.getFirst();

            if (currentCommit.getMaxDistanceFromRoot() == null
                    || currentCommit.getMaxDistanceFromRoot() < currentDistance) {
                currentCommit.setMaxDistanceFromRoot(currentDistance);
            }
            if (!currentCommit.getHeadOfBranches().isEmpty() || currentCommit.getParents().size() > 1 || Objects.equals(
                    rootCommit,
                    currentCommit)) {
                if (group.getId() != null) {
                    group = new GitCommitGroup();
                }

            } else {
                if (group.getId() == null) {
                    group = gitCommitGroupRepository.save(group);
                }
                currentCommit.setGroup(group);
                if (currentCommit.getChildren().size() > 1) {
                    if (group.getId() != null) {
                        group = new GitCommitGroup();
                    }
                }
            }
            currentCommit.getChildren().forEach(x -> stack.push(Pair.of(x.getChild(), currentDistance + 1)));
        }

    }


}
