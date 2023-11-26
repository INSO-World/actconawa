package at.ac.tuwien.inso.actconawa.index.language.java;

import at.ac.tuwien.inso.actconawa.antlr.java.JavaLexer;
import at.ac.tuwien.inso.actconawa.antlr.java.JavaParser;
import at.ac.tuwien.inso.actconawa.exception.IndexingIOException;
import at.ac.tuwien.inso.actconawa.exception.IndexingLanguageParserException;
import at.ac.tuwien.inso.actconawa.index.language.LanguageIndexModule;
import at.ac.tuwien.inso.actconawa.index.language.java.dto.JavaMemberDeclarationInfo;
import at.ac.tuwien.inso.actconawa.index.language.java.persistence.JavaCodeChange;
import at.ac.tuwien.inso.actconawa.index.language.java.persistence.JavaCodeChangeRepository;
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

    private final JavaCodeChangeRepository javaCodeChangeRepository;


    public JavaIndexModule(Git git, JavaCodeChangeRepository javaCodeChangeRepository) {
        this.git = git;
        this.javaCodeChangeRepository = javaCodeChangeRepository;
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
                        var members = MemberDeclarationProcessUtils.processMembers(typeDeclaration);
                        var typeEntity = new JavaCodeChange();
                        typeEntity.setDiffHunk(gitCommitDiffHunk);
                        typeEntity.setType(type.type().name());
                        typeEntity.setIdentifier(type.identifier());
                        typeEntity.setSourceLineStart(type.sourceRange().getMinimum());
                        typeEntity.setSourceLineEnd(type.sourceRange().getMaximum());
                        javaCodeChangeRepository.save(typeEntity);
                        for (JavaMemberDeclarationInfo member : members) {
                            var memberIsInChangeRange = member.sourceRange().isOverlappedBy(changeRange);
                            LOG.debug("member {} is in change range: {}/{}",
                                    member,
                                    memberIsInChangeRange,
                                    changeRange);
                            var memberEntity = new JavaCodeChange();
                            memberEntity.setDiffHunk(gitCommitDiffHunk);
                            memberEntity.setType(member.type().name());
                            memberEntity.setIdentifier(member.identifier());
                            memberEntity.setSourceLineStart(member.sourceRange().getMinimum());
                            memberEntity.setSourceLineEnd(member.sourceRange().getMaximum());
                            if (member.getParamTypeTypes() != null) {
                                memberEntity.setMemberParamTypeTypes(String.join(", ",
                                        member.getParamTypeTypes()));
                            }
                            memberEntity.setMemberTypeType(member.getTypeType());

                            javaCodeChangeRepository.save(memberEntity);
                        }

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
