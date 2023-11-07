package at.ac.tuwien.inso.actconawa.dto;

import java.io.Serializable;
import java.util.UUID;

public class GitBranchDto implements Serializable {

    private UUID id;

    private String name;

    private UUID headCommitId;

    private boolean remoteHead;

    private boolean containingExclusiveCommits;

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

    public UUID getHeadCommitId() {
        return headCommitId;
    }

    public void setHeadCommitId(UUID headCommitId) {
        this.headCommitId = headCommitId;
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
}
