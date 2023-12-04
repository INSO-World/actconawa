package at.ac.tuwien.inso.actconawa.service;

import at.ac.tuwien.inso.actconawa.api.DiffService;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffHunkDto;
import at.ac.tuwien.inso.actconawa.mapper.GitMapper;
import at.ac.tuwien.inso.actconawa.repository.GitCommitDiffHunkRepository;
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

    public static final int DIFF_LINE_CONTEXT = 1;

    private static final Logger LOG = LoggerFactory.getLogger(GitDiffService.class);

    private final Git git;

    private final GitCommitDiffHunkRepository gitCommitDiffHunkRepository;


    private final GitMapper gitMapper;

    public GitDiffService(Git git,
            GitCommitDiffHunkRepository gitCommitDiffHunkRepository,
            GitMapper gitMapper) {
        this.git = git;
        this.gitCommitDiffHunkRepository = gitCommitDiffHunkRepository;
        this.gitMapper = gitMapper;
    }

    @Override
    public String getDiff(RevCommit gitCommit, RevCommit parentCommit) {
        try (var reader = git.getRepository().newObjectReader();
             var outputStream = new ByteArrayOutputStream();
             var formatter = new DiffFormatter(outputStream)
        ) {
            var commitTreeId = gitCommit.getTree().getId();
            var parentCommitTreeId = parentCommit.getTree().getId();

            var commitTree = new CanonicalTreeParser(null, reader, commitTreeId);
            var parentCommitTree = new CanonicalTreeParser(null, reader, parentCommitTreeId);

            formatter.setRepository(git.getRepository());
            // 3 lines are default contecxt in jgit org.eclipse.jgit.diff.DiffFormatter.context
            formatter.setContext(DIFF_LINE_CONTEXT);
            formatter.format(parentCommitTree, commitTree);
            return outputStream.toString();
        } catch (IOException e) {
            LOG.error("Creating diff between {} and {} failed.",
                    gitCommit.getId().getName(),
                    parentCommit.getId().getName(),
                    e);
            return null;
        }
    }

    @Override
    public String getDiff(RevCommit gitCommit) {
        try (var reader = git.getRepository().newObjectReader();
             var outputStream = new ByteArrayOutputStream();
             var formatter = new DiffFormatter(outputStream)
        ) {
            var commitTreeId = gitCommit.getTree().getId();

            var commitTree = new CanonicalTreeParser(null, reader, commitTreeId);

            formatter.setRepository(git.getRepository());
            formatter.format(new EmptyTreeIterator(), commitTree);
            return outputStream.toString();
        } catch (IOException e) {
            LOG.error("Creating diff of commit {} failed.",
                    gitCommit.getId().getName(),
                    e);
            return null;
        }
    }

    @Override
    public List<GitCommitDiffHunkDto> findGitCommitDiffHunksByDiffFileId(UUID commitDiffFileId) {
        return gitCommitDiffHunkRepository.findByCommitDiffFile(commitDiffFileId).stream()
                .map(gitMapper::mapModelToDto)
                .collect(Collectors.toList());
    }

}
