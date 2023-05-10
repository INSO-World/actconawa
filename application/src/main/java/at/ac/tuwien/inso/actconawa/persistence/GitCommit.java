package at.ac.tuwien.inso.actconawa.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class GitCommit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sha;

    private String message;

    private String authorName;

    private String authorEmail;

    private LocalDateTime commitDate;

    @ManyToMany
    @JoinTable(
            name = "commit_parent",
            joinColumns = @JoinColumn(name = "commit_id"),
            inverseJoinColumns = @JoinColumn(name = "parent_id")
    )
    private List<GitCommit> parents;

    public GitCommit() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public List<GitCommit> getParents() {
        return parents;
    }

    public void setParents(List<GitCommit> parents) {
        this.parents = parents;
    }
}
