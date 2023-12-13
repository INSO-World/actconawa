package at.ac.tuwien.inso.actconawa.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "commit_branch", uniqueConstraints = {
        @UniqueConstraint(
                name = "commit_branch_commit_branch_key",
                columnNames = {"commit_id", "branch_id"}
        )
})
public class GitCommitBranch implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "commit_id")
    private GitCommit commit;

    @ManyToOne(optional = false)
    @JoinColumn(name = "branch_id")
    private GitBranch branch;

    public GitCommitBranch() {
    }

    public GitCommitBranch(GitCommit commit, GitBranch branch) {
        this.commit = commit;
        this.branch = branch;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }


    public GitCommit getCommit() {
        return commit;
    }

    public void setCommit(GitCommit commit) {
        this.commit = commit;
    }

    public GitBranch getBranch() {
        return branch;
    }

    public void setBranch(GitBranch branch) {
        this.branch = branch;
    }
}
