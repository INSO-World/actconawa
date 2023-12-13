package at.ac.tuwien.inso.actconawa.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.springframework.context.annotation.Lazy;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "branch")
public class GitBranch implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    private boolean remoteHead;

    private boolean containingExclusiveCommits;

    @ManyToOne
    @JoinColumn(name = "head_commit_id")
    private GitCommit headCommit;

    @Lazy
    @OneToMany(mappedBy = "branch")
    private List<GitCommitBranch> commitBranchRelations;

    public GitBranch() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GitCommit getHeadCommit() {
        return headCommit;
    }

    public void setHeadCommit(GitCommit headCommit) {
        this.headCommit = headCommit;
    }

    public boolean isRemoteHead() {
        return remoteHead;
    }

    public void setRemoteHead(boolean remoteHead) {
        this.remoteHead = remoteHead;
    }

    public boolean isContainingExclusiveCommits() {
        return containingExclusiveCommits;
    }

    public void setContainingExclusiveCommits(boolean containingExclusiveCommits) {
        this.containingExclusiveCommits = containingExclusiveCommits;
    }

    public List<GitCommitBranch> getCommitBranchRelations() {
        return commitBranchRelations;
    }

    public void setCommitBranchRelations(List<GitCommitBranch> commitBranchRelations) {
        this.commitBranchRelations = commitBranchRelations;
    }
}
