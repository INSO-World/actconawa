package at.ac.tuwien.inso.actconawa.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.springframework.context.annotation.Lazy;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "commit")
public class GitCommit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String sha;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String authorName;

    @Column(nullable = false)
    private String authorEmail;

    @Column(nullable = false)
    private LocalDateTime commitDate;

    @Lazy
    @OneToMany(mappedBy = "child", cascade = CascadeType.PERSIST)
    private List<GitCommitRelationship> parents;

    @Lazy
    @OneToMany(mappedBy = "parent")
    private List<GitCommitRelationship> children;

    @Lazy
    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "branch_commit",
            joinColumns = @JoinColumn(name = "commit_id"),
            inverseJoinColumns = @JoinColumn(name = "branch_id"))
    private List<GitBranch> branches;

    public GitCommit() {
    }

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

    public List<GitCommitRelationship> getParents() {
        return parents;
    }

    public void setParents(List<GitCommitRelationship> parents) {
        this.parents = parents;
    }

    public List<GitCommitRelationship> getChildren() {
        return children;
    }

    public void setChildren(List<GitCommitRelationship> children) {
        this.children = children;
    }

    public List<GitBranch> getBranches() {
        return branches;
    }

    public void setBranches(List<GitBranch> branches) {
        this.branches = branches;
    }
}
