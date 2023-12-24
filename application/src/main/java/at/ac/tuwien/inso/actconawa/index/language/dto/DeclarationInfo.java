package at.ac.tuwien.inso.actconawa.index.language.dto;

import at.ac.tuwien.inso.actconawa.index.language.java.dto.DeclarationType;
import org.apache.commons.lang3.IntegerRange;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class DeclarationInfo {

    private final DeclarationType declarationType;

    private final String identifier;

    private final IntegerRange sourceRange;

    private final List<DeclarationInfo> modifiers;

    private final Resolution resolution;


    public DeclarationInfo(
            DeclarationType declarationType,
            String identifier,
            IntegerRange sourceRange,
            List<DeclarationInfo> modifiers,
            Resolution resolution
    ) {
        this.declarationType = declarationType;
        this.identifier = identifier;
        this.sourceRange = sourceRange;
        this.modifiers = modifiers;
        this.resolution = resolution;
    }

    public DeclarationInfo(DeclarationType declarationType,
            String identifier,
            IntegerRange sourceRange,
            Resolution resolution
    ) {
        this.declarationType = declarationType;
        this.identifier = identifier;
        this.sourceRange = sourceRange;
        this.modifiers = new ArrayList<>();
        this.resolution = resolution;
    }

    @Override
    public String toString() {
        return "DeclarationInfo{" +
                "type=" + declarationType +
                ", identifier='" + identifier + '\'' +
                ", sourceRange=" + sourceRange +
                (CollectionUtils.isEmpty(modifiers) ? "" : ", modifiers=" + modifiers) +
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

    public Resolution getResolution() {
        return resolution;
    }
}
