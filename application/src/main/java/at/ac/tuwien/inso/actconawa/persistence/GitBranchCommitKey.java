package at.ac.tuwien.inso.actconawa.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class GitBranchCommitKey implements Serializable {

    @Column(name = "branch_id", nullable = false)
    private Long branch;

    @Column(name = "commit_id", nullable = false)
    private Long commit;

    public Long getBranch() {
        return branch;
    }

    public void setBranch(Long parent) {
        this.branch = parent;
    }

    public Long getCommit() {
        return commit;
    }

    public void setCommit(Long child) {
        this.commit = child;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitBranchCommitKey that = (GitBranchCommitKey) o;
        return Objects.equals(branch, that.branch) && Objects.equals(commit, that.commit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(branch, commit);
    }
}
