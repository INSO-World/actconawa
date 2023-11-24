package at.ac.tuwien.inso.actconawa.index.language.java;

import org.apache.commons.lang3.IntegerRange;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class DeclarationInfo {

    private final DeclarationType declarationType;

    private final String identifier;

    private final IntegerRange sourceRange;

    private final List<DeclarationInfo> contextualExtra;

    public DeclarationInfo(
            DeclarationType declarationType,
            String identifier,
            IntegerRange sourceRange,
            List<DeclarationInfo> contextualExtra
    ) {
        this.declarationType = declarationType;
        this.identifier = identifier;
        this.sourceRange = sourceRange;
        this.contextualExtra = contextualExtra;
    }

    public DeclarationInfo(DeclarationType declarationType, String identifier, IntegerRange sourceRange) {
        this.declarationType = declarationType;
        this.identifier = identifier;
        this.sourceRange = sourceRange;
        this.contextualExtra = List.of();
    }

    @Override
    public String toString() {
        return "DeclarationInfo{" +
                "type=" + declarationType +
                ", identifier='" + identifier + '\'' +
                ", sourceRange=" + sourceRange +
                (CollectionUtils.isEmpty(contextualExtra) ? "" : ", contextualExtra=" + contextualExtra) +
                '}';
    }

    public DeclarationType type() {
        return declarationType;
    }

    public String identifier() {
        return identifier;
    }

    public IntegerRange sourceRange() {
        return sourceRange;
    }

    public List<DeclarationInfo> getContextualExtra() {
        return contextualExtra;
    }

}
