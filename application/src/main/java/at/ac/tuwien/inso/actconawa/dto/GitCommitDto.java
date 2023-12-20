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

    private List<UUID> parentIds;

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

    public List<UUID> getParentIds() {
        return parentIds;
    }

    public void setParentIds(List<UUID> parentIds) {
        this.parentIds = parentIds;
    }
}
