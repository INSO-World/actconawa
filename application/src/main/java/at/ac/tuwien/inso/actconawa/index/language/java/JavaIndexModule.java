package at.ac.tuwien.inso.actconawa.index.language.java;

import at.ac.tuwien.inso.actconawa.antlr.java.JavaLexer;
import at.ac.tuwien.inso.actconawa.antlr.java.JavaParser;
import at.ac.tuwien.inso.actconawa.exception.IndexingIOException;
import at.ac.tuwien.inso.actconawa.exception.IndexingLanguageParserException;
import at.ac.tuwien.inso.actconawa.index.language.LanguageIndexModule;
import at.ac.tuwien.inso.actconawa.index.language.dto.DeclarationInfo;
import at.ac.tuwien.inso.actconawa.index.language.java.dto.DeclarationType;
import at.ac.tuwien.inso.actconawa.index.language.java.dto.JavaMemberDeclarationInfo;
import at.ac.tuwien.inso.actconawa.persistence.CodeChange;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffFile;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffLineChange;
import at.ac.tuwien.inso.actconawa.repository.CodeChangeRepository;
import jakarta.transaction.Transactional;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.IntegerRange;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Component
public class JavaIndexModule implements LanguageIndexModule {

    private static final Logger LOG = LoggerFactory.getLogger(JavaIndexModule.class);

    private static final String PROGRAMMING_LANGUAGE = "Java";

    private final Git git;

    private final CodeChangeRepository codeChangeRepository;


    public JavaIndexModule(Git git, CodeChangeRepository javaCodeChangeRepository) {
        this.git = git;
        this.codeChangeRepository = javaCodeChangeRepository;
    }

    @Transactional
    @Override
    public boolean parseSemanticalDiff(GitCommitDiffFile commitDiffFile) {
        if (!StringUtils.endsWith(commitDiffFile.getNewFilePath(), ".java")) {
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

            CodeChange affectedPackage = null;
            for (var child : cu.children) {
                var lineChanges = Optional.ofNullable(commitDiffFile.getGitCommitDiffLineChanges()).orElse(List.of());
                for (GitCommitDiffLineChange gitCommitDiffLineChange : lineChanges) {
                    var start = gitCommitDiffLineChange.getNewStartLine();
                    var end = gitCommitDiffLineChange.getNewStartLine() + gitCommitDiffLineChange.getNewLineCount();
                    var changeRange = IntegerRange.of(start.intValue(), end);
                    var type = DeclarationProcessUtils.processUnspecificDeclaration(child);
                    var typeIsInChangeRange = type.sourceRange().isOverlappedBy(changeRange);
                    LOG.debug("declaration {} is in change range: {}/{}", type, typeIsInChangeRange, changeRange);

                    var typeEntitiesToSave = new HashSet<CodeChange>();
                    var modifiersToSave = new HashSet<CodeChange>();
                    var membersToSave = new HashSet<CodeChange>();
                    var typeEntity = new CodeChange();

                    typeEntity.setDiffFile(gitCommitDiffLineChange.getDiffFile());
                    typeEntity.setType(type.type().name());
                    typeEntity.setIdentifier(type.identifier());
                    typeEntity.setSourceLineStart(type.sourceRange().getMinimum());
                    typeEntity.setSourceLineEnd(type.sourceRange().getMaximum());
                    typeEntity.setJustContext(!typeIsInChangeRange);
                    typeEntity.setProgrammingLanguage(programmingLanguage());
                    typeEntity.setResolution(type.getResolution());
                    if (typeIsInChangeRange) {
                        typeEntitiesToSave.add(typeEntity);
                    } else if (type.type() == DeclarationType.PACKAGE) {
                        // Package is needed for the context
                        typeEntity.setJustContext(true);
                        typeEntitiesToSave.add(typeEntity);
                    }
                    if (type.type() == DeclarationType.PACKAGE) {
                        affectedPackage = typeEntity;
                    }
                    var changedTypeModifiers = type.getModifiers()
                            .stream()
                            .filter(x -> x.sourceRange().isOverlappedBy(changeRange))
                            .toList();
                    if (!changedTypeModifiers.isEmpty()) {
                        if (!typeEntitiesToSave.contains(typeEntity)) {
                            typeEntitiesToSave.add(typeEntity);
                            typeEntity.setJustContext(true);
                        }
                        for (DeclarationInfo mdi : changedTypeModifiers) {
                            var modifier = new CodeChange();
                            modifier.setParent(typeEntity);
                            modifier.setDiffFile(gitCommitDiffLineChange.getDiffFile());
                            modifier.setType(mdi.type().name());
                            modifier.setIdentifier(mdi.identifier());
                            modifier.setSourceLineStart(mdi.sourceRange().getMinimum());
                            modifier.setSourceLineEnd(mdi.sourceRange().getMaximum());
                            modifier.setProgrammingLanguage(programmingLanguage());
                            modifier.setResolution(mdi.getResolution());
                            modifiersToSave.add(modifier);
                        }
                    }
                    if (child instanceof JavaParser.TypeDeclarationContext typeDeclaration) {
                        // Since package is always the first declaration in a file (if there is a package set)
                        // it is safe to just set the package here as parent.
                        typeEntity.setParent(affectedPackage);
                        var members = MemberDeclarationProcessUtils.processMembers(typeDeclaration);
                        for (JavaMemberDeclarationInfo member : members) {
                            var memberIsInChangeRange = member.sourceRange().isOverlappedBy(changeRange);
                            LOG.debug("member {} is in change range: {}/{}",
                                    member,
                                    memberIsInChangeRange,
                                    changeRange);
                            var memberEntity = new CodeChange();
                            memberEntity.setDiffFile(gitCommitDiffLineChange.getDiffFile());
                            memberEntity.setType(member.type().name());
                            memberEntity.setIdentifier("%s %s%s".formatted(
                                    member.getTypeType(),
                                    member.identifier(),
                                    member.getParamTypeTypes() == null ? "" :
                                            "(" + String.join(", ", member.getParamTypeTypes()) + ")"));
                            memberEntity.setSourceLineStart(member.sourceRange().getMinimum());
                            memberEntity.setSourceLineEnd(member.sourceRange().getMaximum());
                            memberEntity.setParent(typeEntity);
                            memberEntity.setProgrammingLanguage(programmingLanguage());
                            memberEntity.setResolution(member.getResolution());
                            if (!typeEntitiesToSave.contains(typeEntity)) {
                                typeEntitiesToSave.add(typeEntity);
                                // if a member is changed, so is the parent
                                typeEntity.setJustContext(false);
                            }
                            if (memberIsInChangeRange) {
                                membersToSave.add(memberEntity);
                            }
                            var changedMemberModifiers = member.getModifiers()
                                    .stream()
                                    .filter(x -> x.sourceRange().isOverlappedBy(changeRange))
                                    .toList();
                            if (!changedMemberModifiers.isEmpty()) {
                                if (!membersToSave.contains(memberEntity)) {
                                    membersToSave.add(memberEntity);
                                    memberEntity.setJustContext(true);
                                }
                                for (DeclarationInfo mdi : changedMemberModifiers) {
                                    var modifier = new CodeChange();
                                    modifier.setParent(memberEntity);
                                    modifier.setDiffFile(gitCommitDiffLineChange.getDiffFile());
                                    modifier.setType(mdi.type().name());
                                    modifier.setIdentifier(mdi.identifier());
                                    modifier.setSourceLineStart(mdi.sourceRange().getMinimum());
                                    modifier.setSourceLineEnd(mdi.sourceRange().getMaximum());
                                    modifier.setProgrammingLanguage(programmingLanguage());
                                    modifier.setResolution(mdi.getResolution());
                                    modifiersToSave.add(modifier);
                                }
                            }
                        }

                    }
                    codeChangeRepository.saveAll(typeEntitiesToSave);
                    codeChangeRepository.saveAll(modifiersToSave);
                    codeChangeRepository.saveAll(membersToSave);
                    if (!typeEntitiesToSave.isEmpty() || !modifiersToSave.isEmpty() || !membersToSave.isEmpty()) {
                        break;
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

    @Override
    public String programmingLanguage() {
        return PROGRAMMING_LANGUAGE;
    }
}
