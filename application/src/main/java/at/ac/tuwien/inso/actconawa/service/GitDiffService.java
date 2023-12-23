package at.ac.tuwien.inso.actconawa.service;

import at.ac.tuwien.inso.actconawa.api.DiffService;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffCodeChangeDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffHunkDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffLineChangeDto;
import at.ac.tuwien.inso.actconawa.mapper.GitMapper;
import at.ac.tuwien.inso.actconawa.repository.CodeChangeRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitDiffHunkRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitDiffLineChangeRepository;
import jakarta.transaction.Transactional;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GitDiffService implements DiffService {
    private static final Logger LOG = LoggerFactory.getLogger(GitDiffService.class);

    private final Git git;

    private final GitCommitDiffHunkRepository gitCommitDiffHunkRepository;

    private final GitCommitDiffLineChangeRepository gitCommitDiffLineChangeRepository;

    private final CodeChangeRepository codeChangeRepository;

    private final GitCommitService gitCommitService;


    private final GitMapper gitMapper;

    public GitDiffService(
            Git git,
            GitCommitDiffHunkRepository gitCommitDiffHunkRepository,
            GitCommitDiffLineChangeRepository gitCommitDiffLineChangeRepository, CodeChangeRepository codeChangeRepository, GitCommitService gitCommitService,
            GitMapper gitMapper
    ) {
        this.git = git;
        this.gitCommitDiffHunkRepository = gitCommitDiffHunkRepository;
        this.gitCommitDiffLineChangeRepository = gitCommitDiffLineChangeRepository;
        this.codeChangeRepository = codeChangeRepository;
        this.gitCommitService = gitCommitService;
        this.gitMapper = gitMapper;
    }

    @Override
    public String getDiff(RevCommit gitCommit, RevCommit parentCommit, int contextLines) {
        try (var reader = git.getRepository().newObjectReader();
             var outputStream = new ByteArrayOutputStream();
             var formatter = new DiffFormatter(outputStream)
        ) {
            var commitTreeId = gitCommit.getTree().getId();
            var commitTree = new CanonicalTreeParser(null, reader, commitTreeId);

            var parentCommitTree = parentCommit == null ?
                    new EmptyTreeIterator() :
                    new CanonicalTreeParser(null, reader, parentCommit.getTree().getId());

            formatter.setRepository(git.getRepository());
            formatter.setContext(Math.min(0, contextLines));
            formatter.format(parentCommitTree, commitTree);
            return outputStream.toString();
        } catch (IOException e) {
            LOG.error("Creating diff of {} {} {} failed.",
                    gitCommit.getId().getName(),
                    parentCommit == null ? "(root)" : "and",
                    parentCommit == null ? "" : parentCommit.getId().getName(),
                    e);
            return null;
        }
    }

    @Override
    public String getDiff(UUID commitId, UUID parentCommitId, int contextLines) {
        LOG.info("Creating diff of {} {} {}",
                commitId,
                parentCommitId == null ? "(root)" : "and",
                parentCommitId == null ? "" : parentCommitId);
        var commit = gitCommitService.getRevCommitByGitCommitId(commitId);
        var parentCommit = parentCommitId == null ? null : gitCommitService.getRevCommitByGitCommitId(parentCommitId);
        return getDiff(commit, parentCommit, contextLines);

    }

    @Override
    @Transactional
    public List<GitCommitDiffHunkDto> findGitCommitDiffHunksByDiffFileId(UUID commitDiffFileId) {
        return gitCommitDiffHunkRepository.findByCommitDiffFile(commitDiffFileId).stream()
                .map(gitMapper::mapModelToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<GitCommitDiffLineChangeDto> findGitCommitLineChangesByDiffFileId(UUID commitDiffFileId) {
        return gitCommitDiffLineChangeRepository.findByCommitDiffFile(commitDiffFileId).stream()
                .map(gitMapper::mapModelToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<GitCommitDiffCodeChangeDto> findGitCommitCodeChangesByDiffFileId(UUID commitDiffFileId) {
        return codeChangeRepository.findByCommitDiffFile(commitDiffFileId).stream()
                .map(gitMapper::mapModelToDto)
                .collect(Collectors.toList());
    }

}
