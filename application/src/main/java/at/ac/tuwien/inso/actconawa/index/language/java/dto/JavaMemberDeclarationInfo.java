package at.ac.tuwien.inso.actconawa.index.language.java.dto;

import at.ac.tuwien.inso.actconawa.index.language.dto.DeclarationInfo;
import at.ac.tuwien.inso.actconawa.index.language.dto.Resolution;
import org.apache.commons.lang3.IntegerRange;

import java.util.List;

public class JavaMemberDeclarationInfo extends DeclarationInfo {

    private final String typeType;

    private final List<String> paramTypeTypes;


    public JavaMemberDeclarationInfo(DeclarationType declarationType,
            String identifier,
            IntegerRange sourceRange,
            Resolution resolution,
            String typeType,
            List<String> paramTypeTypes) {
        super(declarationType, identifier, sourceRange, resolution);
        this.typeType = typeType;
        this.paramTypeTypes = paramTypeTypes;
    }

    public static JavaMemberDeclarationInfo of(DeclarationInfo declarationInfo) {
        String typeType = null;
        List<String> paramTypeTypes = null;
        if (declarationInfo instanceof JavaMemberDeclarationInfo javaMemberDeclarationInfo) {
            typeType = javaMemberDeclarationInfo.typeType;
            paramTypeTypes = javaMemberDeclarationInfo.paramTypeTypes;
        }
        return new JavaMemberDeclarationInfo(declarationInfo.type(),
                declarationInfo.identifier(),
                declarationInfo.sourceRange(),
                declarationInfo.getResolution(),
                typeType,
                paramTypeTypes);
    }

    public String getTypeType() {
        return typeType;
    }

    public List<String> getParamTypeTypes() {
        return paramTypeTypes;
    }
}
