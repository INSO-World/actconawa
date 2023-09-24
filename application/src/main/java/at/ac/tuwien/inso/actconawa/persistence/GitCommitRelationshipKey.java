package at.ac.tuwien.inso.actconawa.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class GitCommitRelationshipKey implements Serializable {

    @Column(name = "parent_id", nullable = false)
    private UUID parent;

    @Column(name = "child_id", nullable = false)
    private UUID child;

    public GitCommitRelationshipKey() {
    }

    public GitCommitRelationshipKey(UUID parent, UUID child) {
        this.parent = parent;
        this.child = child;
    }

    public UUID getParent() {
        return parent;
    }

    public void setParent(UUID parent) {
        this.parent = parent;
    }

    public UUID getChild() {
        return child;
    }

    public void setChild(UUID child) {
        this.child = child;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitCommitRelationshipKey that = (GitCommitRelationshipKey) o;
        return Objects.equals(parent, that.parent) && Objects.equals(child, that.child);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, child);
    }
}
