package at.ac.tuwien.inso.actconawa.index.language.java;

import org.apache.commons.lang3.IntegerRange;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Optional;

public class JavaMemberDeclarationInfo extends DeclarationInfo {

    private final MemberDeclaration memberDeclarationType;

    private final String type;

    private final Pair<Optional<String>, List<String>> paramTypes;


    public JavaMemberDeclarationInfo(DeclarationType declarationType, String identifier, IntegerRange sourceRange, List<DeclarationInfo> contextualExtra, MemberDeclaration memberDeclarationType, String type, Pair<Optional<String>, List<String>> paramTypes) {
        super(declarationType, identifier, sourceRange, contextualExtra);
        this.memberDeclarationType = memberDeclarationType;
        this.type = type;
        this.paramTypes = paramTypes;
    }

    public JavaMemberDeclarationInfo(DeclarationType declarationType, String identifier, IntegerRange sourceRange, MemberDeclaration memberDeclarationType, String type, Pair<Optional<String>, List<String>> paramTypes) {
        super(declarationType, identifier, sourceRange);
        this.memberDeclarationType = memberDeclarationType;
        this.type = type;
        this.paramTypes = paramTypes;
    }
}
