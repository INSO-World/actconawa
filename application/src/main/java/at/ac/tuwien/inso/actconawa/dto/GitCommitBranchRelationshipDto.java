package at.ac.tuwien.inso.actconawa.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class GitCommitBranchRelationshipDto implements Serializable {

    private UUID commitId;

    private List<UUID> branchIds;

    public UUID getCommitId() {
        return commitId;
    }

    public void setCommitId(UUID commitId) {
        this.commitId = commitId;
    }

    public List<UUID> getBranchIds() {
        return branchIds;
    }

    public void setBranchIds(List<UUID> branchIds) {
        this.branchIds = branchIds;
    }
}
