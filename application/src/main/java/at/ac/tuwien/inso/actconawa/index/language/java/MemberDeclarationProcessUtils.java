package at.ac.tuwien.inso.actconawa.index.language.java;

import at.ac.tuwien.inso.actconawa.antlr.java.JavaParser;
import org.antlr.v4.runtime.RuleContext;
import org.apache.commons.lang3.IntegerRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class MemberDeclarationProcessUtils {

    private static final Logger LOG = LoggerFactory.getLogger(MemberDeclarationProcessUtils.class);

    private MemberDeclarationProcessUtils() {
    }

    static void processMembers(JavaParser.TypeDeclarationContext typeDeclaration) {
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
        Stream.of(classMembers, enumMembers, recordMembers).flatMap(Collection::stream).forEach(x -> {
            // TODO: support annotations
            // classBodyDeclaration
            //    : ';'
            //    | STATIC? block
            //    | modifier* memberDeclaration
            //    ;
            // where modifier contains variables
            processMethodDeclaration(x.methodDeclaration());
            if (x.genericMethodDeclaration() != null) {
                processMethodDeclaration(x.genericMethodDeclaration().methodDeclaration());
            }
            processFieldDeclaration(x.fieldDeclaration());
            processConstructorDeclaration(x.constructorDeclaration());
            DeclarationProcessUtils.processTypeDeclaration(x.classDeclaration(), List.of());
            DeclarationProcessUtils.processTypeDeclaration(x.interfaceDeclaration(), List.of());
            DeclarationProcessUtils.processTypeDeclaration(x.enumDeclaration(), List.of());
            DeclarationProcessUtils.processTypeDeclaration(x.annotationTypeDeclaration(), List.of());
            DeclarationProcessUtils.processTypeDeclaration(x.recordDeclaration(), List.of());
        });
        interfaceMembers.forEach(x -> {
            processInterfaceMethodDeclaration(x.interfaceMethodDeclaration());
            processGenericInterfaceMethodDeclaration(x.genericInterfaceMethodDeclaration());
            processConstDeclaration(x.constDeclaration());
            DeclarationProcessUtils.processTypeDeclaration(x.classDeclaration(), List.of());
            DeclarationProcessUtils.processTypeDeclaration(x.interfaceDeclaration(), List.of());
            DeclarationProcessUtils.processTypeDeclaration(x.enumDeclaration(), List.of());
            DeclarationProcessUtils.processTypeDeclaration(x.annotationTypeDeclaration(), List.of());
            DeclarationProcessUtils.processTypeDeclaration(x.recordDeclaration(), List.of());
        });
        annotationTypeMembers.forEach(x -> {
            // TODO : x.annotationMethodOrConstantRest()....
            DeclarationProcessUtils.processTypeDeclaration(x.classDeclaration(), List.of());
            DeclarationProcessUtils.processTypeDeclaration(x.interfaceDeclaration(), List.of());
            DeclarationProcessUtils.processTypeDeclaration(x.enumDeclaration(), List.of());
            DeclarationProcessUtils.processTypeDeclaration(x.annotationTypeDeclaration(), List.of());
            DeclarationProcessUtils.processTypeDeclaration(x.recordDeclaration(), List.of());

        });

    }


    private static void processAnnotation(JavaParser.AnnotationContext ctx) {
        if (ctx == null) {
            return;
        }
        var txt = ctx.qualifiedName().getText();
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        LOG.debug("annotation {} @ {}", txt, range);
    }

    private static void processMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        if (ctx == null) {
            return;
        }
        var methodName = ctx.identifier().getText();
        var methodReturnType = ctx.typeTypeOrVoid().getText();
        var formalParameters = readFormalParameterContext(ctx.formalParameters());
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        LOG.debug("method {} {} with parameters {} @ {}", methodReturnType, methodName, formalParameters, range);
    }


    private static void processInterfaceMethodDeclaration(JavaParser.InterfaceMethodDeclarationContext ctx) {
        if (ctx == null) {
            return;
        }
        var methodName = ctx.interfaceCommonBodyDeclaration().identifier().getText();
        var methodReturnType = ctx.interfaceCommonBodyDeclaration().typeTypeOrVoid().getText();
        var formalParameters = readFormalParameterContext(ctx.interfaceCommonBodyDeclaration().formalParameters());
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        LOG.debug("interface method {} {} with parameters {} @ {}",
                methodReturnType,
                methodName,
                formalParameters,
                range);
    }

    private static void processGenericInterfaceMethodDeclaration(JavaParser.GenericInterfaceMethodDeclarationContext ctx) {
        if (ctx == null) {
            return;
        }
        var methodName = ctx.interfaceCommonBodyDeclaration().identifier().getText();
        var methodReturnType = ctx.interfaceCommonBodyDeclaration().typeTypeOrVoid().getText();
        var formalParameters = readFormalParameterContext(ctx.interfaceCommonBodyDeclaration().formalParameters());
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        LOG.debug("generic interface method {} {} with parameters {} @ {}",
                methodReturnType,
                methodName,
                formalParameters,
                range);
    }

    private static void processConstDeclaration(JavaParser.ConstDeclarationContext ctx) {
        if (ctx == null) {
            return;
        }
        var type = ctx.typeType().getText();
        var identifiers = ctx.constantDeclarator()
                .stream()
                .map(JavaParser.ConstantDeclaratorContext::identifier)
                .map(JavaParser.IdentifierContext::getText)
                .toString();
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        LOG.debug("const {} {} @ {}", type, identifiers, range);
    }

    private static void processFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        if (ctx == null) {
            return;
        }
        var typeText = ctx.typeType().getText();
        var fieldNames = ctx.variableDeclarators()
                .variableDeclarator()
                .stream()
                .map(JavaParser.VariableDeclaratorContext::variableDeclaratorId)
                .map(JavaParser.VariableDeclaratorIdContext::identifier)
                .map(RuleContext::getText)
                .toList();
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        LOG.debug("field type {} with ids {} @ {}", typeText, fieldNames, range);
    }

    private static void processConstructorDeclaration(JavaParser.ConstructorDeclarationContext ctx) {
        if (ctx == null) {
            return;
        }
        var identifier = ctx.identifier().getText();
        var formalParameters = readFormalParameterContext(ctx.formalParameters());
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());

        LOG.debug("constructor {} and parameters {} @ {}", identifier, formalParameters, range);
    }

    private static Pair<Optional<String>, List<String>> readFormalParameterContext(JavaParser.FormalParametersContext ctx) {
        var receiverParameterType = Optional.ofNullable(ctx.receiverParameter()).map(x -> x.typeType().getText());
        var formalParameterTypes = Optional.ofNullable(ctx.formalParameterList())
                .map(JavaParser.FormalParameterListContext::formalParameter)
                .orElse(List.of())
                .stream()
                .map(JavaParser.FormalParameterContext::typeType)
                .map(RuleContext::getText)
                .toList();
        return Pair.of(receiverParameterType, formalParameterTypes);
    }

}
