package at.ac.tuwien.inso.actconawa.dto;

import java.util.UUID;

public class GitBranchTrackingStatusDto {

    private UUID id;

    private UUID branchAId;

    private UUID branchBId;

    private UUID mergeBaseCommitId;

    private int aheadCount;

    private int behindCount;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBranchAId() {
        return branchAId;
    }

    public void setBranchAId(UUID branchAId) {
        this.branchAId = branchAId;
    }

    public UUID getBranchBId() {
        return branchBId;
    }

    public void setBranchBId(UUID branchBId) {
        this.branchBId = branchBId;
    }

    public UUID getMergeBaseCommitId() {
        return mergeBaseCommitId;
    }

    public void setMergeBaseCommitId(UUID mergeBaseCommitId) {
        this.mergeBaseCommitId = mergeBaseCommitId;
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
