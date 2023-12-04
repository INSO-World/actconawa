package at.ac.tuwien.inso.actconawa.index.language.java;

import at.ac.tuwien.inso.actconawa.antlr.java.JavaParser;
import at.ac.tuwien.inso.actconawa.index.language.java.dto.DeclarationInfo;
import at.ac.tuwien.inso.actconawa.index.language.java.dto.DeclarationType;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.IntegerRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeclarationProcessUtils {

    private DeclarationProcessUtils() {
    }

    public static DeclarationInfo processUnspecificDeclaration(ParseTree tree) {
        if (tree instanceof JavaParser.ImportDeclarationContext ctx) {
            return processDeclaration(ctx);
        } else if (tree instanceof JavaParser.PackageDeclarationContext ctx) {
            return processDeclaration(ctx);
        } else if (tree instanceof JavaParser.ModuleDeclarationContext ctx) {
            return processDeclaration(ctx);
        } else if (tree instanceof JavaParser.TypeDeclarationContext ctx) {
            DeclarationInfo result = null;
            List<DeclarationInfo> annotations = new ArrayList<>();
            for (var ctxChild : ctx.children) {
                if (ctxChild == null) {
                    continue;
                }
                if (ctxChild instanceof JavaParser.ClassOrInterfaceModifierContext modifierContext) {
                    annotations.addAll(getAnnotations(modifierContext.children));
                }
                if (ctxChild instanceof JavaParser.ClassDeclarationContext typeDeclaration) {
                    result = processTypeDeclaration(typeDeclaration, annotations);
                    break;
                } else if (ctxChild instanceof JavaParser.InterfaceDeclarationContext typeDeclaration) {
                    result = processTypeDeclaration(typeDeclaration, annotations);
                    break;
                } else if (ctxChild instanceof JavaParser.EnumDeclarationContext typeDeclaration) {
                    result = processTypeDeclaration(typeDeclaration, annotations);
                    break;
                } else if (ctxChild instanceof JavaParser.AnnotationTypeDeclarationContext typeDeclaration) {
                    result = processTypeDeclaration(typeDeclaration, annotations);
                    break;
                } else if (ctxChild instanceof JavaParser.RecordDeclarationContext typeDeclaration) {
                    result = processTypeDeclaration(typeDeclaration, annotations);
                    break;
                }
            }
            if (result == null) {
                return new DeclarationInfo(DeclarationType.UNKNOWN_TYPE,
                        ctx.getText(),
                        IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine()));
            }
            return result;
        }
        throw new IllegalArgumentException("Unexpected tree");
    }


    static DeclarationInfo processTypeDeclaration(JavaParser.ClassDeclarationContext ctx) {
        return processTypeDeclaration(ctx, List.of());
    }

    static DeclarationInfo processTypeDeclaration(JavaParser.InterfaceDeclarationContext ctx) {
        return processTypeDeclaration(ctx, List.of());
    }

    static DeclarationInfo processTypeDeclaration(JavaParser.EnumDeclarationContext ctx) {
        return processTypeDeclaration(ctx, List.of());
    }

    static DeclarationInfo processTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) {
        return processTypeDeclaration(ctx, List.of());
    }

    static DeclarationInfo processTypeDeclaration(JavaParser.RecordDeclarationContext ctx) {
        return processTypeDeclaration(ctx, List.of());
    }

    static DeclarationInfo processTypeDeclaration(JavaParser.ClassDeclarationContext ctx, List<DeclarationInfo> annotations) {
        if (ctx == null) {
            return null;
        }
        var identifier = ctx.identifier().getText();
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        return new DeclarationInfo(DeclarationType.CLASS, identifier, range, annotations);
    }

    static DeclarationInfo processTypeDeclaration(JavaParser.InterfaceDeclarationContext ctx, List<DeclarationInfo> annotations) {
        if (ctx == null) {
            return null;
        }
        var identifier = ctx.identifier().getText();
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        return new DeclarationInfo(DeclarationType.INTERFACE, identifier, range, annotations);
    }

    static DeclarationInfo processTypeDeclaration(JavaParser.EnumDeclarationContext ctx, List<DeclarationInfo> annotations) {
        if (ctx == null) {
            return null;
        }
        var identifier = ctx.identifier().getText();
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        return new DeclarationInfo(DeclarationType.ENUM, identifier, range, annotations);
    }

    static DeclarationInfo processTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx, List<DeclarationInfo> annotations) {
        if (ctx == null) {
            return null;
        }
        var identifier = ctx.identifier().getText();
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        return new DeclarationInfo(DeclarationType.ANNOTATION_TYPE, identifier, range, annotations);
    }

    static DeclarationInfo processTypeDeclaration(JavaParser.RecordDeclarationContext ctx, List<DeclarationInfo> annotations) {
        if (ctx == null) {
            return null;
        }
        var identifier = ctx.identifier().getText();
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        return new DeclarationInfo(DeclarationType.RECORD, identifier, range, annotations);
    }

    private static DeclarationInfo processDeclaration(JavaParser.ImportDeclarationContext ctx) {
        var identifier = ctx.qualifiedName().getText();
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        return new DeclarationInfo(DeclarationType.IMPORT, identifier, range, getAnnotations(ctx.children));
    }

    private static DeclarationInfo processDeclaration(JavaParser.PackageDeclarationContext ctx) {
        var identifier = ctx.qualifiedName().getText();
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());
        return new DeclarationInfo(DeclarationType.PACKAGE, identifier, range, getAnnotations(ctx.children));

    }

    private static DeclarationInfo processDeclaration(JavaParser.ModuleDeclarationContext ctx) {
        var identifier = ctx.qualifiedName().getText();
        var range = IntegerRange.of(ctx.getStart().getLine(), ctx.getStop().getLine());

        return new DeclarationInfo(DeclarationType.MODULE, identifier, range, getAnnotations(ctx.children));

    }

    private static List<DeclarationInfo> getAnnotations(List<ParseTree> trees) {
        return trees.stream()
                .filter(Objects::nonNull)
                .filter(x -> x instanceof JavaParser.AnnotationContext)
                .map(x -> (JavaParser.AnnotationContext) x)
                .map(x -> new DeclarationInfo(
                        DeclarationType.ANNOTATION,
                        x.qualifiedName().getText(),
                        IntegerRange.of(x.start.getLine(), x.stop.getLine())))
                .toList();
    }
}
