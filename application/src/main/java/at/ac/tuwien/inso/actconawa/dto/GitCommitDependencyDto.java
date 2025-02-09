package at.ac.tuwien.inso.actconawa.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class GitCommitDependencyDto implements Serializable {

    private UUID commitId;

    private List<UUID> commitDependencyIds;

    public UUID getCommitId() {
        return commitId;
    }

    public void setCommitId(UUID commitId) {
        this.commitId = commitId;
    }

    public List<UUID> getCommitDependencyIds() {
        return commitDependencyIds;
    }

    public void setCommitDependencyIds(List<UUID> commitDependencyIds) {
        this.commitDependencyIds = commitDependencyIds;
    }
}
