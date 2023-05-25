package at.ac.tuwien.inso.actconawa.persistence;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import org.springframework.context.annotation.Lazy;

import java.io.Serializable;

@Entity
@Table(name = "branch_commit")
public class GitBranchCommit implements Serializable {

    @EmbeddedId
    private GitBranchCommitKey id;

    @Lazy
    @ManyToOne
    @MapsId("branch_id")
    private GitBranch branch;

    @Lazy
    @ManyToOne
    @MapsId("commit_id")
    private GitCommit commit;


    public GitBranchCommitKey getId() {
        return id;
    }

    public void setId(GitBranchCommitKey id) {
        this.id = id;
    }

    public GitBranch getBranch() {
        return branch;
    }

    public void setBranch(GitBranch branch) {
        this.branch = branch;
    }

    public GitCommit getCommit() {
        return commit;
    }

    public void setCommit(GitCommit commit) {
        this.commit = commit;
    }
}
