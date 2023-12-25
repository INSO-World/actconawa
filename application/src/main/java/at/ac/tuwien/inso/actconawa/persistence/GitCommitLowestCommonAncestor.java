package at.ac.tuwien.inso.actconawa.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "commit_lca")
public class GitCommitLowestCommonAncestor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "commit_a")
    private GitCommit commitA;

    @ManyToOne
    @JoinColumn(name = "commit_b")
    private GitCommit commitB;

    @ManyToOne
    @JoinColumn(name = "lca")
    private GitCommit lowestCommonAncestorCommit;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public GitCommit getCommitA() {
        return commitA;
    }

    public void setCommitA(GitCommit commitA) {
        this.commitA = commitA;
    }

    public GitCommit getCommitB() {
        return commitB;
    }

    public void setCommitB(GitCommit commitB) {
        this.commitB = commitB;
    }

    public GitCommit getLowestCommonAncestorCommit() {
        return lowestCommonAncestorCommit;
    }

    public void setLowestCommonAncestorCommit(GitCommit lowestCommonAncestorCommit) {
        this.lowestCommonAncestorCommit = lowestCommonAncestorCommit;
    }
}
