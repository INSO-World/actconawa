package at.ac.tuwien.inso.actconawa.index.language.java;

import at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.AnnotationContext;
import at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.ClassBodyDeclarationContext;
import at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.InterfaceMemberDeclarationContext;
import at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.MemberDeclarationContext;
import at.ac.tuwien.inso.actconawa.index.language.java.dto.DeclarationType;
import at.ac.tuwien.inso.actconawa.index.language.java.dto.JavaMemberDeclarationInfo;
import org.antlr.v4.runtime.RuleContext;
import org.apache.commons.lang3.IntegerRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.AnnotationTypeElementDeclarationContext;
import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.ClassOrInterfaceModifierContext;
import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.ConstDeclarationContext;
import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.ConstantDeclaratorContext;
import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.ConstructorDeclarationContext;
import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.FieldDeclarationContext;
import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.FormalParameterContext;
import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.FormalParameterListContext;
import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.FormalParametersContext;
import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.GenericInterfaceMethodDeclarationContext;
import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.GenericMethodDeclarationContext;
import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.IdentifierContext;
import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.InterfaceBodyDeclarationContext;
import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.InterfaceMethodDeclarationContext;
import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.MethodDeclarationContext;
import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.ModifierContext;
import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.TypeDeclarationContext;
import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.VariableDeclaratorContext;
import static at.ac.tuwien.inso.actconawa.antlr.java.JavaParser.VariableDeclaratorIdContext;

public class MemberDeclarationProcessUtils {

    private static final Logger LOG = LoggerFactory.getLogger(MemberDeclarationProcessUtils.class);

    private MemberDeclarationProcessUtils() {
    }

    static List<JavaMemberDeclarationInfo> processMembers(TypeDeclarationContext typeDeclaration) {

        // only exactly one of the interfaceMembers/classMembers/enumMembers/recordMembers/annotationTypeMembers
        // will contain elements depending on which type the typeDeclaration is.
        if (typeDeclaration.interfaceDeclaration() != null) {
            var interfaceMembers = Optional.ofNullable(typeDeclaration.interfaceDeclaration())
                    .flatMap(x -> Optional.ofNullable(x.interfaceBody()))
                    .map(x -> x.interfaceBodyDeclaration().stream())
                    .orElse(Stream.of())
                    .filter(x -> x.interfaceMemberDeclaration() != null)
                    .map(MemberDeclarationProcessUtils::retrieveMemberAndAnnotations)
                    .toList();
            return processInterfaceBodyMembers(interfaceMembers);
        }
        if (typeDeclaration.classDeclaration() != null) {
            var classMembers = Optional.ofNullable(typeDeclaration.classDeclaration())
                    .flatMap(x -> Optional.ofNullable(x.classBody()))
                    .map(x -> x.classBodyDeclaration().stream())
                    .orElse(Stream.of())
                    .filter(x -> x.memberDeclaration() != null)
                    .map(MemberDeclarationProcessUtils::retrieveMemberAndAnnotations)
                    .toList();
            return processClassBodyMembers(classMembers);
        }
        if (typeDeclaration.enumDeclaration() != null) {
            var enumMembers = Optional.ofNullable(typeDeclaration.enumDeclaration())
                    .flatMap(x -> Optional.ofNullable(x.enumBodyDeclarations()))
                    .map(x -> x.classBodyDeclaration().stream())
                    .orElse(Stream.of())
                    .filter(x -> x.memberDeclaration() != null)
                    .map(MemberDeclarationProcessUtils::retrieveMemberAndAnnotations)
                    .toList();
            return processClassBodyMembers(enumMembers);
        }
        if (typeDeclaration.recordDeclaration() != null) {
            var recordMembers = Optional.ofNullable(typeDeclaration.recordDeclaration())
                    .flatMap(x -> Optional.ofNullable(x.recordBody()))
                    .map(x -> x.classBodyDeclaration().stream())
                    .orElse(Stream.of())
                    .filter(x -> x.memberDeclaration() != null)
                    .map(MemberDeclarationProcessUtils::retrieveMemberAndAnnotations)
                    .toList();
            return processClassBodyMembers(recordMembers);
        }
        if (typeDeclaration.annotationTypeDeclaration() != null) {
            // Also known as Annotation-Interface
            var annotationTypeMembers = Optional.ofNullable(typeDeclaration.annotationTypeDeclaration())
                    .flatMap(x -> Optional.ofNullable(x.annotationTypeBody()))
                    .map(x -> x.annotationTypeElementDeclaration().stream())
                    .orElse(Stream.of())
                    .filter(x -> x.annotationTypeElementRest() != null)
                    .map(AnnotationTypeElementDeclarationContext::annotationTypeElementRest)
                    .toList();
            return annotationTypeMembers.stream().flatMap(x -> Stream.of(
                            // TODO : x.annotationMethodOrConstantRest()....
                            DeclarationProcessUtils.processTypeDeclaration(x.classDeclaration(), List.of()),
                            DeclarationProcessUtils.processTypeDeclaration(x.interfaceDeclaration(), List.of()),
                            DeclarationProcessUtils.processTypeDeclaration(x.enumDeclaration(), List.of()),
                            DeclarationProcessUtils.processTypeDeclaration(x.annotationTypeDeclaration(), List.of()),
                            DeclarationProcessUtils.processTypeDeclaration(x.recordDeclaration(), List.of()))
                    .filter(Objects::nonNull).map(JavaMemberDeclarationInfo::of)
            ).toList();


        }
        return new ArrayList<>();
    }

    private static List<JavaMemberDeclarationInfo> processClassBodyMembers(List<Pair<List<JavaMemberDeclarationInfo>, MemberDeclarationContext>> context) {
        return context.stream().map(x ->
                Stream.of(Optional.ofNullable(x.getSecond().genericMethodDeclaration())
                                        .map(GenericMethodDeclarationContext::methodDeclaration)
                                        .map(MemberDeclarationProcessUtils::processMethodDeclaration)
                                        .orElse(null),
                                processMethodDeclaration(x.getSecond().methodDeclaration()),
                                processFieldDeclaration(x.getSecond().fieldDeclaration()),
                                processConstructorDeclaration(x.getSecond().constructorDeclaration()),
                                DeclarationProcessUtils.processTypeDeclaration(x.getSecond().classDeclaration()),
                                DeclarationProcessUtils.processTypeDeclaration(x.getSecond().interfaceDeclaration()),
                                DeclarationProcessUtils.processTypeDeclaration(x.getSecond().enumDeclaration(), List.of()),
                                DeclarationProcessUtils.processTypeDeclaration(x.getSecond().annotationTypeDeclaration()),
                                DeclarationProcessUtils.processTypeDeclaration(x.getSecond().recordDeclaration()))
                        .filter(Objects::nonNull)
                        .findFirst()
                        .map(JavaMemberDeclarationInfo::of)
                        .map(member -> {
                            member.getModifiers().addAll(x.getFirst());
                            return member;
                        }).orElse(null)
        ).filter(Objects::nonNull).toList();
    }

    private static List<JavaMemberDeclarationInfo> processInterfaceBodyMembers(List<Pair<List<JavaMemberDeclarationInfo>, InterfaceMemberDeclarationContext>> context) {
        return context.stream().flatMap(x -> Stream.of(
                        processInterfaceMethodDeclaration(x.getSecond().interfaceMethodDeclaration()),
                        processGenericInterfaceMethodDeclaration(x.getSecond().genericInterfaceMethodDeclaration()),
                        processConstDeclaration(x.getSecond().constDeclaration()),
                        DeclarationProcessUtils.processTypeDeclaration(x.getSecond().classDeclaration()),
                        DeclarationProcessUtils.processTypeDeclaration(x.getSecond().interfaceDeclaration()),
                        DeclarationProcessUtils.processTypeDeclaration(x.getSecond().enumDeclaration()),
                        DeclarationProcessUtils.processTypeDeclaration(x.getSecond().annotationTypeDeclaration()),
                        DeclarationProcessUtils.processTypeDeclaration(x.getSecond().recordDeclaration()))
                .filter(Objects::nonNull)
                .map(JavaMemberDeclarationInfo::of)
                .map(member -> {
                    member.getModifiers().addAll(x.getFirst());
                    return member;
                })
        ).toList();
    }

    private static JavaMemberDeclarationInfo processAnnotation(AnnotationContext ctx) {
        if (ctx == null) {
            return null;
        }
        var identifier = ctx.qualifiedName().getText();
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        return new JavaMemberDeclarationInfo(DeclarationType.ANNOTATION,
                identifier,
                range,
                null,
                null);
    }

    private static JavaMemberDeclarationInfo processMethodDeclaration(MethodDeclarationContext ctx) {
        if (ctx == null) {
            return null;
        }
        var methodName = ctx.identifier().getText();
        var methodReturnType = ctx.typeTypeOrVoid().getText();
        var formalParameters = readFormalParameterContext(ctx.formalParameters());
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        return new JavaMemberDeclarationInfo(DeclarationType.METHOD,
                methodName,
                range,
                methodReturnType,
                formalParameters);
    }


    private static JavaMemberDeclarationInfo processInterfaceMethodDeclaration(InterfaceMethodDeclarationContext ctx) {
        if (ctx == null) {
            return null;
        }
        var methodName = ctx.interfaceCommonBodyDeclaration().identifier().getText();
        var methodReturnType = ctx.interfaceCommonBodyDeclaration().typeTypeOrVoid().getText();
        var formalParameters = readFormalParameterContext(ctx.interfaceCommonBodyDeclaration().formalParameters());
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        return new JavaMemberDeclarationInfo(DeclarationType.METHOD,
                methodName,
                range,
                methodReturnType,
                formalParameters);
    }

    private static JavaMemberDeclarationInfo processGenericInterfaceMethodDeclaration(GenericInterfaceMethodDeclarationContext ctx) {
        if (ctx == null) {
            return null;
        }
        var methodName = ctx.interfaceCommonBodyDeclaration().identifier().getText();
        var methodReturnType = ctx.interfaceCommonBodyDeclaration().typeTypeOrVoid().getText();
        var formalParameters = readFormalParameterContext(ctx.interfaceCommonBodyDeclaration().formalParameters());
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        return new JavaMemberDeclarationInfo(DeclarationType.METHOD,
                methodName,
                range,
                methodReturnType,
                formalParameters);
    }

    private static JavaMemberDeclarationInfo processConstDeclaration(ConstDeclarationContext ctx) {
        if (ctx == null) {
            return null;
        }
        var type = ctx.typeType().getText();
        var identifiers = ctx.constantDeclarator()
                .stream()
                .map(ConstantDeclaratorContext::identifier)
                .map(IdentifierContext::getText)
                .toString();
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        return new JavaMemberDeclarationInfo(DeclarationType.CONST, identifiers, range, type, null);
    }

    private static JavaMemberDeclarationInfo processFieldDeclaration(FieldDeclarationContext ctx) {
        if (ctx == null) {
            return null;
        }
        var typeText = ctx.typeType().getText();
        var fieldNames = ctx.variableDeclarators()
                .variableDeclarator()
                .stream()
                .map(VariableDeclaratorContext::variableDeclaratorId)
                .map(VariableDeclaratorIdContext::identifier)
                .map(RuleContext::getText)
                .collect(Collectors.joining(", "));
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        return new JavaMemberDeclarationInfo(DeclarationType.FIELD, fieldNames, range, typeText, null);

    }

    private static JavaMemberDeclarationInfo processConstructorDeclaration(ConstructorDeclarationContext ctx) {
        if (ctx == null) {
            return null;
        }
        var identifier = ctx.identifier().getText();
        var formalParameters = readFormalParameterContext(ctx.formalParameters());
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        return new JavaMemberDeclarationInfo(DeclarationType.CONSTRUCTOR, identifier, range, null, formalParameters);
    }

    /**
     * Returns the formal parameters of a method or constructor. Receiver parameters as described here
     * <a href="https://docs.oracle.com/javase/specs/jls/se20/html/jls-8.html#jls-8.4">
     * Java Language and Virtual Machine Specifications
     * </a> are ignored, as they do not really provide extra-value for this use case.
     *
     * @return a list of the parameter types names.
     */
    private static List<String> readFormalParameterContext(FormalParametersContext ctx) {
        return Optional.ofNullable(ctx.formalParameterList())
                .map(FormalParameterListContext::formalParameter)
                .orElse(List.of())
                .stream()
                .map(FormalParameterContext::typeType)
                .map(RuleContext::getText)
                .toList();
    }

    private static Pair<List<JavaMemberDeclarationInfo>, MemberDeclarationContext> retrieveMemberAndAnnotations(
            ClassBodyDeclarationContext classBodyDeclarationContext
    ) {
        return Pair.of(
                Optional.ofNullable(classBodyDeclarationContext.modifier()).orElse(List.of()).stream()
                        .map(ModifierContext::classOrInterfaceModifier)
                        .filter(Objects::nonNull)
                        .map(ClassOrInterfaceModifierContext::annotation)
                        .filter(Objects::nonNull)
                        .map(MemberDeclarationProcessUtils::processAnnotation)
                        .toList(),
                classBodyDeclarationContext.memberDeclaration());
    }

    private static Pair<List<JavaMemberDeclarationInfo>, InterfaceMemberDeclarationContext> retrieveMemberAndAnnotations(
            InterfaceBodyDeclarationContext interfaceBodyDeclarationContext
    ) {
        return Pair.of(
                interfaceBodyDeclarationContext.modifier().stream()
                        .map(ModifierContext::classOrInterfaceModifier)
                        .filter(Objects::nonNull)
                        .map(ClassOrInterfaceModifierContext::annotation)
                        .filter(Objects::nonNull)
                        .map(MemberDeclarationProcessUtils::processAnnotation)
                        .toList(),
                interfaceBodyDeclarationContext.interfaceMemberDeclaration());
    }
}
