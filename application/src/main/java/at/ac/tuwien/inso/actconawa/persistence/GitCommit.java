package at.ac.tuwien.inso.actconawa.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.springframework.context.annotation.Lazy;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "commit")
public class GitCommit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    @OneToMany(mappedBy = "parent", cascade = CascadeType.PERSIST)
    private List<GitCommitRelationship> parents;

    @Lazy
    @OneToMany(mappedBy = "child")
    private List<GitCommitRelationship> children;

    @Lazy
    @OneToMany(mappedBy = "commit")
    private List<GitBranchCommit> branchRelationships;

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

    public List<GitBranchCommit> getBranchRelationships() {
        return branchRelationships;
    }

    public void setBranchRelationships(List<GitBranchCommit> branches) {
        this.branchRelationships = branches;
    }
}
