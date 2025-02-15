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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Order(2)
/**
 * Index grouping information of commits
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
        }
        collectCommitGroups(rootCommits.get(0));

    }

    @Override
    public String getIndexedContentDescription() {
        return "commit grouping infos";
    }

    private void collectCommitGroups(GitCommit rootCommit) {
        if (rootCommit == null) {
            return;
        }
        Stack<Pair<GitCommit, GitCommitGroup>> stack = new Stack<>();
        stack.add(Pair.of(rootCommit, new GitCommitGroup()));
        Set<UUID> visited = new HashSet<>();
        var groupMap = new HashMap<GitCommitGroup, HashSet<GitCommit>>();

        var commits = gitCommitRepository.findAll();
        LOG.debug("Indexing grouping of {} commits", commits.size());
        var commitsMap = commits.stream().collect(Collectors.toMap(GitCommit::getId, commit -> commit));
        while (!stack.isEmpty()) {
            Pair<GitCommit, GitCommitGroup> current = stack.pop();
            GitCommitGroup group = current.getSecond();
            GitCommit currentCommit = current.getFirst();

            if (visited.contains(currentCommit.getId())) {
                continue;
            }
            visited.add(currentCommit.getId());

            LOG.debug("Grouping commits {}", currentCommit.getId());

            var childCommitsIds = gitCommitRepository.findChildCommitIdsOfCommit(currentCommit);
            var parentCommitsIds = gitCommitRepository.findParentCommitIdsOfCommit(currentCommit);
            if (!currentCommit.getHeadOfBranches().isEmpty()
                    || parentCommitsIds.size() > 1
                    || Objects.equals(rootCommit, currentCommit)
            ) {
                childCommitsIds.forEach(x -> stack.push(Pair.of(commitsMap.get(x), new GitCommitGroup())));
            } else {
                if (!groupMap.containsKey(group)) {
                    groupMap.put(group, new HashSet<>());
                }
                groupMap.get(group).add(currentCommit);
                if (childCommitsIds.size() > 1) {
                    childCommitsIds.forEach(x -> stack.push(Pair.of(commitsMap.get(x), new GitCommitGroup())));
                } else {
                    for (UUID x : childCommitsIds) {
                        stack.push(Pair.of(commitsMap.get(x), group));
                    }
                }
            }
        }

        groupMap.forEach((group, commitSet) -> {
            if (commitSet.size() > 1) {
                var persistedGroup = gitCommitGroupRepository.save(group);
                commitSet.forEach(commit -> commit.setGroup(persistedGroup));
            }
        });
    }


}
