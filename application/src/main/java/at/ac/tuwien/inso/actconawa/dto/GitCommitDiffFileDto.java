package at.ac.tuwien.inso.actconawa.dto;

import java.io.Serializable;
import java.util.UUID;

public class GitCommitDiffFileDto implements Serializable {

    private UUID id;

    private String newFilePath;

    private String oldFilePath;

    private String changeType;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNewFilePath() {
        return newFilePath;
    }

    public void setNewFilePath(String newFilePath) {
        this.newFilePath = newFilePath;
    }

    public String getOldFilePath() {
        return oldFilePath;
    }

    public void setOldFilePath(String oldFilePath) {
        this.oldFilePath = oldFilePath;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }
}
