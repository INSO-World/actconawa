package at.ac.tuwien.inso.actconawa.persistence;

import at.ac.tuwien.inso.actconawa.enums.MergeStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.List;
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

    @Column(name = "merge_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MergeStatus mergeStatus;


    @Column(name = "ahead_count", nullable = false)
    private int aheadCount;

    @Column(name = "behind_count", nullable = false)
    private int behindCount;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "merge_conflict_file_path", joinColumns = @JoinColumn(name = "branch_tracking_status_id"))
    @Column(name = "merge_conflict_file_path", nullable = false)
    private List<String> conflictingFilePaths;

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

    public MergeStatus getMergeStatus() {
        return mergeStatus;
    }

    public void setMergeStatus(MergeStatus mergeStatus) {
        this.mergeStatus = mergeStatus;
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

    public List<String> getConflictingFilePaths() {
        return conflictingFilePaths;
    }

    public void setConflictingFilePaths(List<String> conflictingFilePaths) {
        this.conflictingFilePaths = conflictingFilePaths;
    }
}
