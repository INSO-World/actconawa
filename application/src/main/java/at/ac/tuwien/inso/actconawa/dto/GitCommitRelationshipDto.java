package at.ac.tuwien.inso.actconawa.dto;

import java.io.Serializable;
import java.util.UUID;

public class GitCommitRelationshipDto implements Serializable {

    private UUID id;

    private UUID parentId;

    private UUID childId;

    public GitCommitRelationshipDto() {
    }

    public GitCommitRelationshipDto(UUID id, UUID parentId, UUID childId) {
        this.id = id;
        this.parentId = parentId;
        this.childId = childId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public UUID getChildId() {
        return childId;
    }

    public void setChildId(UUID childId) {
        this.childId = childId;
    }
}
