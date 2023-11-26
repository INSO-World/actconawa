package at.ac.tuwien.inso.actconawa.index.language.java.persistence;

import at.ac.tuwien.inso.actconawa.persistence.CodeChange;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity(name = "java_code_change")
public class JavaCodeChange extends CodeChange {

    @Column(name = "member_type_type")
    private String memberTypeType;

    @Column(name = "param_type_types")
    private String memberParamTypeTypes;

    public String getMemberTypeType() {
        return memberTypeType;
    }

    public void setMemberTypeType(String memberTypeType) {
        this.memberTypeType = memberTypeType;
    }

    public String getMemberParamTypeTypes() {
        return memberParamTypeTypes;
    }

    public void setMemberParamTypeTypes(String memberParamTypeTypes) {
        this.memberParamTypeTypes = memberParamTypeTypes;
    }
}
