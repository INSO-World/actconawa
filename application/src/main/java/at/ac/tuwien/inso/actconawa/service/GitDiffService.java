package at.ac.tuwien.inso.actconawa.service;

import at.ac.tuwien.inso.actconawa.api.DiffService;
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

@Service
public class GitDiffService implements DiffService {

    private static final Logger LOG = LoggerFactory.getLogger(GitDiffService.class);

    private final Git git;

    public GitDiffService(Git git) {
        this.git = git;
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

}
