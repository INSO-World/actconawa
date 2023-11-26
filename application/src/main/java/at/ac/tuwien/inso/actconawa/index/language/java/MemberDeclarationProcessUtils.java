package at.ac.tuwien.inso.actconawa.index.language.java;

import at.ac.tuwien.inso.actconawa.antlr.java.JavaParser;
import at.ac.tuwien.inso.actconawa.index.language.java.dto.DeclarationType;
import at.ac.tuwien.inso.actconawa.index.language.java.dto.JavaMemberDeclarationInfo;
import org.antlr.v4.runtime.RuleContext;
import org.apache.commons.lang3.IntegerRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MemberDeclarationProcessUtils {

    private static final Logger LOG = LoggerFactory.getLogger(MemberDeclarationProcessUtils.class);

    private MemberDeclarationProcessUtils() {
    }

    static List<JavaMemberDeclarationInfo> processMembers(JavaParser.TypeDeclarationContext typeDeclaration) {
        var interfaceMembers = Optional.ofNullable(typeDeclaration.interfaceDeclaration())
                .flatMap(x -> Optional.ofNullable(x.interfaceBody()))
                .map(x -> x.interfaceBodyDeclaration().stream())
                .orElse(Stream.of())
                .map(JavaParser.InterfaceBodyDeclarationContext::interfaceMemberDeclaration)
                .toList();
        var classMembers = Optional.ofNullable(typeDeclaration.classDeclaration())
                .flatMap(x -> Optional.ofNullable(x.classBody()))
                .map(x -> x.classBodyDeclaration().stream())
                .orElse(Stream.of())
                .map(JavaParser.ClassBodyDeclarationContext::memberDeclaration)
                .toList();
        var enumMembers = Optional.ofNullable(typeDeclaration.enumDeclaration())
                .flatMap(x -> Optional.ofNullable(x.enumBodyDeclarations()))
                .map(x -> x.classBodyDeclaration().stream())
                .orElse(Stream.of())
                .map(JavaParser.ClassBodyDeclarationContext::memberDeclaration)
                .toList();
        var recordMembers = Optional.ofNullable(typeDeclaration.recordDeclaration())
                .flatMap(x -> Optional.ofNullable(x.recordBody()))
                .map(x -> x.classBodyDeclaration().stream())
                .orElse(Stream.of())
                .map(JavaParser.ClassBodyDeclarationContext::memberDeclaration)
                .toList();
        var annotationTypeMembers = Optional.ofNullable(typeDeclaration.annotationTypeDeclaration())
                .flatMap(x -> Optional.ofNullable(x.annotationTypeBody()))
                .map(x -> x.annotationTypeElementDeclaration().stream())
                .orElse(Stream.of())
                .map(JavaParser.AnnotationTypeElementDeclarationContext::annotationTypeElementRest)
                .toList();
        var classes = Stream.of(classMembers, enumMembers, recordMembers).flatMap(Collection::stream).flatMap(x ->
                // TODO: support annotations
                // classBodyDeclaration
                //    : ';'
                //    | STATIC? block
                //    | modifier* memberDeclaration
                //    ;
                // where modifier contains variables
                Stream.of(
                        Optional.ofNullable(x.genericMethodDeclaration())
                                .map(JavaParser.GenericMethodDeclarationContext::methodDeclaration)
                                .map(MemberDeclarationProcessUtils::processMethodDeclaration)
                                .orElse(null),
                        processMethodDeclaration(x.methodDeclaration()),
                        processFieldDeclaration(x.fieldDeclaration()),
                        processConstructorDeclaration(x.constructorDeclaration()),
                        DeclarationProcessUtils.processTypeDeclaration(x.classDeclaration(), List.of()),
                        DeclarationProcessUtils.processTypeDeclaration(x.interfaceDeclaration(), List.of()),
                        DeclarationProcessUtils.processTypeDeclaration(x.enumDeclaration(), List.of()),
                        DeclarationProcessUtils.processTypeDeclaration(x.annotationTypeDeclaration(), List.of()),
                        DeclarationProcessUtils.processTypeDeclaration(x.recordDeclaration(), List.of())
                ).filter(Objects::nonNull).map(JavaMemberDeclarationInfo::of)
        ).toList();
        var interfaces = interfaceMembers.stream().flatMap(x -> Stream.of(
                        processInterfaceMethodDeclaration(x.interfaceMethodDeclaration()),
                        processGenericInterfaceMethodDeclaration(x.genericInterfaceMethodDeclaration()),
                        processConstDeclaration(x.constDeclaration()),
                        DeclarationProcessUtils.processTypeDeclaration(x.classDeclaration(), List.of()),
                        DeclarationProcessUtils.processTypeDeclaration(x.interfaceDeclaration(), List.of()),
                        DeclarationProcessUtils.processTypeDeclaration(x.enumDeclaration(), List.of()),
                        DeclarationProcessUtils.processTypeDeclaration(x.annotationTypeDeclaration(), List.of()),
                        DeclarationProcessUtils.processTypeDeclaration(x.recordDeclaration(), List.of())
                ).filter(Objects::nonNull).map(JavaMemberDeclarationInfo::of)
        ).toList();
        var annotationTypes = annotationTypeMembers.stream().flatMap(x -> Stream.of(
                        // TODO : x.annotationMethodOrConstantRest()....
                        DeclarationProcessUtils.processTypeDeclaration(x.classDeclaration(), List.of()),
                        DeclarationProcessUtils.processTypeDeclaration(x.interfaceDeclaration(), List.of()),
                        DeclarationProcessUtils.processTypeDeclaration(x.enumDeclaration(), List.of()),
                        DeclarationProcessUtils.processTypeDeclaration(x.annotationTypeDeclaration(), List.of()),
                        DeclarationProcessUtils.processTypeDeclaration(x.recordDeclaration(), List.of()))
                .filter(Objects::nonNull).map(JavaMemberDeclarationInfo::of)
        ).toList();
        var result = new ArrayList<JavaMemberDeclarationInfo>();
        result.addAll(classes);
        result.addAll(interfaces);
        result.addAll(annotationTypes);
        return result;

    }


    private static void processAnnotation(JavaParser.AnnotationContext ctx) {
        if (ctx == null) {
            return;
        }
        var txt = ctx.qualifiedName().getText();
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        LOG.debug("annotation {} @ {}", txt, range);
    }

    private static JavaMemberDeclarationInfo processMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
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


    private static JavaMemberDeclarationInfo processInterfaceMethodDeclaration(JavaParser.InterfaceMethodDeclarationContext ctx) {
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

    private static JavaMemberDeclarationInfo processGenericInterfaceMethodDeclaration(JavaParser.GenericInterfaceMethodDeclarationContext ctx) {
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

    private static JavaMemberDeclarationInfo processConstDeclaration(JavaParser.ConstDeclarationContext ctx) {
        if (ctx == null) {
            return null;
        }
        var type = ctx.typeType().getText();
        var identifiers = ctx.constantDeclarator()
                .stream()
                .map(JavaParser.ConstantDeclaratorContext::identifier)
                .map(JavaParser.IdentifierContext::getText)
                .toString();
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        return new JavaMemberDeclarationInfo(DeclarationType.CONST, identifiers, range, type, null);
    }

    private static JavaMemberDeclarationInfo processFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        if (ctx == null) {
            return null;
        }
        var typeText = ctx.typeType().getText();
        var fieldNames = ctx.variableDeclarators()
                .variableDeclarator()
                .stream()
                .map(JavaParser.VariableDeclaratorContext::variableDeclaratorId)
                .map(JavaParser.VariableDeclaratorIdContext::identifier)
                .map(RuleContext::getText)
                .collect(Collectors.joining(", "));
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        return new JavaMemberDeclarationInfo(DeclarationType.FIELD, fieldNames, range, typeText, null);

    }

    private static JavaMemberDeclarationInfo processConstructorDeclaration(JavaParser.ConstructorDeclarationContext ctx) {
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
    private static List<String> readFormalParameterContext(JavaParser.FormalParametersContext ctx) {
        return Optional.ofNullable(ctx.formalParameterList())
                .map(JavaParser.FormalParameterListContext::formalParameter)
                .orElse(List.of())
                .stream()
                .map(JavaParser.FormalParameterContext::typeType)
                .map(RuleContext::getText)
                .toList();
    }

}
