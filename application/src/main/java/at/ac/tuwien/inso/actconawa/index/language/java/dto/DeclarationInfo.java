package at.ac.tuwien.inso.actconawa.index.language.java.dto;

import org.apache.commons.lang3.IntegerRange;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class DeclarationInfo {

    private final DeclarationType declarationType;

    private final String identifier;

    private final IntegerRange sourceRange;

    private final List<DeclarationInfo> modifiers;

    private final List<DeclarationInfo> subElements;

    public DeclarationInfo(
            DeclarationType declarationType,
            String identifier,
            IntegerRange sourceRange,
            List<DeclarationInfo> modifiers
    ) {
        this.declarationType = declarationType;
        this.identifier = identifier;
        this.sourceRange = sourceRange;
        this.modifiers = modifiers;
        this.subElements = new ArrayList<>();
    }

    public DeclarationInfo(DeclarationType declarationType, String identifier, IntegerRange sourceRange) {
        this.declarationType = declarationType;
        this.identifier = identifier;
        this.sourceRange = sourceRange;
        this.modifiers = new ArrayList<>();
        this.subElements = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "DeclarationInfo{" +
                "type=" + declarationType +
                ", identifier='" + identifier + '\'' +
                ", sourceRange=" + sourceRange +
                (CollectionUtils.isEmpty(modifiers) ? "" : ", modifiers=" + modifiers) +
                (CollectionUtils.isEmpty(subElements) ? "" : ", subElements=" + subElements) +
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

    public List<DeclarationInfo> getModifiers() {
        return modifiers;
    }

}
