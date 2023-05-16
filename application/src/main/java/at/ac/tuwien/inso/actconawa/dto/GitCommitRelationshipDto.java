package at.ac.tuwien.inso.actconawa.dto;


public class GitCommitRelationshipDto {

    private Long parentId;

    private Long childId;

    public GitCommitRelationshipDto() {
    }

    public GitCommitRelationshipDto(Long parentId, Long childId) {
        this.parentId = parentId;
        this.childId = childId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getChildId() {
        return childId;
    }

    public void setChildId(Long childId) {
        this.childId = childId;
    }
}
