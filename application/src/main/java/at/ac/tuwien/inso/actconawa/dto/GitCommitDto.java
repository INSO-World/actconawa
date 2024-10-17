package at.ac.tuwien.inso.actconawa.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public class GitCommitDto implements Serializable {

    private UUID id;

    private String sha;

    private String message;

    private String authorName;

    private String authorEmail;

    private LocalDateTime commitDate;

    private UUID groupId;

    private List<UUID> headOfBranchesIds;

    private List<UUID> parentIds;

    private List<UUID> childIds;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public LocalDateTime getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(LocalDateTime commitDate) {
        this.commitDate = commitDate;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public List<UUID> getHeadOfBranchesIds() {
        return headOfBranchesIds;
    }

    public void setHeadOfBranchesIds(List<UUID> headOfBranchesIds) {
        this.headOfBranchesIds = headOfBranchesIds;
    }

    public List<UUID> getParentIds() {
        return parentIds;
    }

    public void setParentIds(List<UUID> parentIds) {
        this.parentIds = parentIds;
    }

    public List<UUID> getChildIds() {
        return childIds;
    }

    public void setChildIds(List<UUID> childIds) {
        this.childIds = childIds;
    }
}
