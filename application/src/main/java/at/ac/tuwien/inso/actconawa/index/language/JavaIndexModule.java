package at.ac.tuwien.inso.actconawa.index.language;

import at.ac.tuwien.inso.actconawa.exception.IndexingIOException;
import at.ac.tuwien.inso.actconawa.exception.IndexingLanguageParserException;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffFile;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffHunk;
import com.github.javaparser.JavaParser;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.TypeDeclaration;
import jakarta.transaction.Transactional;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.github.javaparser.ParseStart.COMPILATION_UNIT;
import static com.github.javaparser.Providers.provider;

@Component
public class JavaIndexModule implements LanguageIndexModule {

    private static final Logger LOG = LoggerFactory.getLogger(JavaIndexModule.class);

    private final Git git;

    private final JavaParser javaParser;

    public JavaIndexModule(Git git) {
        this.git = git;
        this.javaParser = new JavaParser();
    }

    @Transactional
    @Override
    public boolean parseSemanticalDiff(GitCommitDiffFile commitDiffFile) {
        if (!commitDiffFile.getNewFilePath().endsWith(".java")) {
            return false;
        }
        try (var or = git.getRepository().newObjectReader()) {
            var ol = or.open(ObjectId.fromString(commitDiffFile.getNewFileObjectId()));
            var file = new String(ol.getBytes());
            LOG.debug("Successfully loaded file {} at {}",
                    commitDiffFile.getNewFilePath(),
                    commitDiffFile.getCommitRelationship().getChild().getSha());

            javaParser.parse(COMPILATION_UNIT, provider(file)).ifSuccessful(cu -> {
                for (TypeDeclaration<?> typeDeclaration : cu.getTypes()) {
                    var hunks = Optional.ofNullable(commitDiffFile.getGitCommitDiffHunks()).orElse(List.of());
                    for (GitCommitDiffHunk gitCommitDiffHunk : hunks) {
                        var start = gitCommitDiffHunk.getNewStartLine();
                        var end = gitCommitDiffHunk.getNewStartLine() + gitCommitDiffHunk.getNewLineCount();
                        LOG.debug("Find whats semantically happening between {} and {}", start, end);
                        // TODO: More than depth == 1?
                        typeDeclaration.getChildNodes().stream()
                                .map(x -> x.getClass().getName() + " " + x.getRange())
                                .forEach(LOG::debug);
                        for (Node astNode : typeDeclaration.getChildNodes()) {
                            // Check for beginning of the changed line
                            var checkStart = new Position(start, 0);
                            // Check till the end of the line
                            var checkEnd = new Position(end, Integer.MAX_VALUE);
                            // Compose the range that is used to identify overlaps with the AST Node
                            var checkRange = new Range(checkStart, checkEnd);
                            var isInRange = astNode.getRange()
                                    .map(range -> range.overlapsWith(checkRange))
                                    .orElse(false);
                            LOG.debug("{} {} is in range {}", astNode.getClass().getSimpleName(), astNode, isInRange);
                        }
                    }
                }
            });
        } catch (MissingObjectException e) {
            throw new IndexingLanguageParserException(e);
        } catch (IOException e) {
            throw new IndexingIOException(e);
        }
        return true;
    }
}
