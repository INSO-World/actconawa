package at.ac.tuwien.inso.actconawa.persistence;

import jakarta.persistence.Column;
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
@Table(name = "branch_tracking_status")
public class GitBranchTrackingStatus implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "branch_a")
    private GitBranch branchA;

    @ManyToOne
    @JoinColumn(name = "branch_b")
    private GitBranch branchB;

    @ManyToOne
    @JoinColumn(name = "merge_base")
    private GitCommit mergeBase;

    @Column(name = "is_merged_into", nullable = false)
    private boolean isMergedInto;

    @Column(name = "ahead_count", nullable = false)
    private int aheadCount;

    @Column(name = "behind_count", nullable = false)
    private int behindCount;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public GitBranch getBranchA() {
        return branchA;
    }

    public void setBranchA(GitBranch branchA) {
        this.branchA = branchA;
    }

    public GitBranch getBranchB() {
        return branchB;
    }

    public void setBranchB(GitBranch branchB) {
        this.branchB = branchB;
    }

    public GitCommit getMergeBase() {
        return mergeBase;
    }

    public void setMergeBase(GitCommit mergeBase) {
        this.mergeBase = mergeBase;
    }

    public boolean isMergedInto() {
        return isMergedInto;
    }

    public void setMergedInto(boolean mergedInto) {
        isMergedInto = mergedInto;
    }

    public int getAheadCount() {
        return aheadCount;
    }

    public void setAheadCount(int aheadCount) {
        this.aheadCount = aheadCount;
    }

    public int getBehindCount() {
        return behindCount;
    }

    public void setBehindCount(int behindCount) {
        this.behindCount = behindCount;
    }
}
