package at.ac.tuwien.inso.actconawa.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class GitCommitRelationshipKey implements Serializable {

    @Column(name = "parent_id", nullable = false)
    private Long parent;

    @Column(name = "child_id", nullable = false)
    private Long child;

    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }

    public Long getChild() {
        return child;
    }

    public void setChild(Long child) {
        this.child = child;
    }
}
