package at.ac.tuwien.inso.actconawa.index.language.java.dto;

import org.apache.commons.lang3.IntegerRange;

import java.util.List;

public class JavaMemberDeclarationInfo extends DeclarationInfo {

    private final String typeType;

    private final List<String> paramTypeTypes;


    public JavaMemberDeclarationInfo(DeclarationType declarationType, String identifier, IntegerRange sourceRange, List<DeclarationInfo> contextualExtra, String typeType, List<String> paramTypeTypes) {
        super(declarationType, identifier, sourceRange, contextualExtra);
        this.typeType = typeType;
        this.paramTypeTypes = paramTypeTypes;
    }

    public JavaMemberDeclarationInfo(DeclarationType declarationType, String identifier, IntegerRange sourceRange, String typeType, List<String> paramTypeTypes) {
        super(declarationType, identifier, sourceRange);
        this.typeType = typeType;
        this.paramTypeTypes = paramTypeTypes;
    }

    public String getTypeType() {
        return typeType;
    }

    public List<String> getParamTypeTypes() {
        return paramTypeTypes;
    }
}
