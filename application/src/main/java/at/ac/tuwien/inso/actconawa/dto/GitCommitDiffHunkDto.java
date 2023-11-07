package at.ac.tuwien.inso.actconawa.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class GitCommitDiffHunkDto implements Serializable {

    private UUID id;

    private Integer newStartLine;

    private Integer newLineCount;

    private Integer oldStartLine;

    private Integer oldLineCount;

    private UUID diffFileId;

    private List<UUID> commitDependencyIds;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getNewStartLine() {
        return newStartLine;
    }

    public void setNewStartLine(Integer newStartLine) {
        this.newStartLine = newStartLine;
    }

    public Integer getNewLineCount() {
        return newLineCount;
    }

    public void setNewLineCount(Integer newLineCount) {
        this.newLineCount = newLineCount;
    }

    public Integer getOldStartLine() {
        return oldStartLine;
    }

    public void setOldStartLine(Integer oldStartLine) {
        this.oldStartLine = oldStartLine;
    }

    public Integer getOldLineCount() {
        return oldLineCount;
    }

    public void setOldLineCount(Integer oldLineCount) {
        this.oldLineCount = oldLineCount;
    }

    public UUID getDiffFileId() {
        return diffFileId;
    }

    public void setDiffFileId(UUID diffFileId) {
        this.diffFileId = diffFileId;
    }

    public List<UUID> getCommitDependencyIds() {
        return commitDependencyIds;
    }

    public void setCommitDependencyIds(List<UUID> commitDependencyIds) {
        this.commitDependencyIds = commitDependencyIds;
    }
}
