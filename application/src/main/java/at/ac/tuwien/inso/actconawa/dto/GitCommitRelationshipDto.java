package at.ac.tuwien.inso.actconawa.dto;


import java.util.UUID;

public class GitCommitRelationshipDto {

    private UUID parentId;

    private UUID childId;

    public GitCommitRelationshipDto() {
    }

    public GitCommitRelationshipDto(UUID parentId, UUID childId) {
        this.parentId = parentId;
        this.childId = childId;
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
