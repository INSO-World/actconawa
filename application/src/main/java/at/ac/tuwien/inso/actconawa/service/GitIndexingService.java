package at.ac.tuwien.inso.actconawa.service;

import at.ac.tuwien.inso.actconawa.persistence.GitBranch;
import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import at.ac.tuwien.inso.actconawa.repository.GitBranchRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.revwalk.RevCommit;
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

    private final GitBranchRepository gitBranchRepository;

    private final GitCommitRepository gitCommitRepository;

    @Value("${actconawa.repo}")
    private String repo;

    public GitIndexingService(GitBranchRepository gitBranchRepository,
            GitCommitRepository gitCommitRepository) {
        this.gitBranchRepository = gitBranchRepository;
        this.gitCommitRepository = gitCommitRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void index() throws IOException {
        var repository = new FileRepository(repo);
        Git git = null;
        try {
            git = new Git(repository);
            var commitCache = new HashMap<String, GitCommit>();
            for (RevCommit commit : git.log().all().call()) {
                GitCommit gitCommit = commitCache.computeIfAbsent(commit.getName(),
                        (k) -> new GitCommit());
                gitCommit.setSha(commit.getName());
                gitCommit.setMessage(commit.getShortMessage()
                        .substring(0, Math.min(commit.getShortMessage().length(), 255)));
                gitCommit.setAuthorName(commit.getAuthorIdent().getName());
                gitCommit.setAuthorEmail(commit.getAuthorIdent().getEmailAddress());
                // https://bugs.eclipse.org/bugs/show_bug.cgi?id=319142
                gitCommit.setCommitDate(LocalDateTime.ofEpochSecond(commit.getCommitTime(),
                        0,
                        ZoneOffset.UTC));
                Arrays.stream(commit.getParents()).forEach(x -> {
                            var parent = commitCache.computeIfAbsent(x.getId().getName(),
                                    (k) -> new GitCommit());
                            if (gitCommit.getParents() == null) {
                                gitCommit.setParents(new ArrayList<>());
                            }
                            gitCommit.getParents().add(parent);
                        }
                );
            }
            gitCommitRepository.saveAll(commitCache.values());

            String[] prefixes = repository.getRemoteNames()
                    .stream()
                    .map("refs/remotes/%s"::formatted)
                    .toArray(String[]::new);
            var branches = repository.getRefDatabase()
                    .getRefsByPrefix(prefixes)
                    .stream()
                    .filter(x -> !x.isSymbolic())
                    .map(x -> {
                        var gitBranch = new GitBranch();
                        gitBranch.setName(StringUtils.removeStart(x.getName(), "refs/remotes/"));
                        gitBranch.setHeadCommit(commitCache.get(x.getObjectId().getName()));
                        return gitBranch;
                    })
                    .collect(Collectors.toList());
            gitBranchRepository.saveAll(branches);
        } catch (GitAPIException e) {
            // TODO: Exception Handling
        } finally {
            if (git != null) {
                git.close();
            }
        }

    }
}
