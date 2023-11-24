package at.ac.tuwien.inso.actconawa.index.language.java;

import at.ac.tuwien.inso.actconawa.antlr.java.JavaLexer;
import at.ac.tuwien.inso.actconawa.antlr.java.JavaParser;
import at.ac.tuwien.inso.actconawa.exception.IndexingIOException;
import at.ac.tuwien.inso.actconawa.exception.IndexingLanguageParserException;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffFile;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffHunk;
import jakarta.transaction.Transactional;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.IntegerRange;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JavaIndexModule implements LanguageIndexModule {

    private static final Logger LOG = LoggerFactory.getLogger(JavaIndexModule.class);

    private final Git git;


    public JavaIndexModule(Git git) {
        this.git = git;
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

            JavaLexer java8Lexer = new JavaLexer(CharStreams.fromString(file));
            CommonTokenStream tokens = new CommonTokenStream(java8Lexer);
            var aparser = new at.ac.tuwien.inso.actconawa.antlr.java.JavaParser(tokens);
            JavaParser.CompilationUnitContext cu = aparser.compilationUnit();

            for (var child : cu.children) {
                var hunks = Optional.ofNullable(commitDiffFile.getGitCommitDiffHunks()).orElse(List.of());
                for (GitCommitDiffHunk gitCommitDiffHunk : hunks) {
                    var start = gitCommitDiffHunk.getNewStartLine();
                    var end = gitCommitDiffHunk.getNewStartLine() + gitCommitDiffHunk.getNewLineCount();
                    var changeRange = IntegerRange.of(start.intValue(), end);
                    var type = DeclarationProcessUtils.processUnspecificDeclaration(child);
                    var typeIsInChangeRange = type.sourceRange().isOverlappedBy(changeRange);
                    LOG.debug("declaration {} is in change range: {}/{}", type, typeIsInChangeRange, changeRange);
                    if (child instanceof JavaParser.TypeDeclarationContext typeDeclaration) {
                        MemberDeclarationProcessUtils.processMembers(typeDeclaration);
                        /* TODO: Return list of members. Check if Range fits and persist.

                                var isInRange = member()
                                    .map(range -> range.overlapsWith(checkRange))
                                    .orElse(false);

                                var memberIsInChangeRange = member.sourceRange().isOverlappedBy(changeRange);
                         }

                         */
                    }
                }
            }

        } catch (MissingObjectException e) {
            throw new IndexingLanguageParserException(e);
        } catch (IOException e) {
            throw new IndexingIOException(e);
        }
        return true;
    }
}
