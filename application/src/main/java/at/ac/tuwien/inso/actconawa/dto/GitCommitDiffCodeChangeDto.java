package at.ac.tuwien.inso.actconawa.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class GitCommitDiffCodeChangeDto implements Serializable {

    private UUID id;

    private String type;

    private String identifier;

    private int sourceLineStart;

    private int sourceLineEnd;

    private boolean justContext;

    private UUID parentId;

    private List<UUID> childrenIds;

    private UUID diffFileId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getSourceLineStart() {
        return sourceLineStart;
    }

    public void setSourceLineStart(int sourceLineStart) {
        this.sourceLineStart = sourceLineStart;
    }

    public int getSourceLineEnd() {
        return sourceLineEnd;
    }

    public void setSourceLineEnd(int sourceLineEnd) {
        this.sourceLineEnd = sourceLineEnd;
    }

    public boolean isJustContext() {
        return justContext;
    }

    public void setJustContext(boolean justContext) {
        this.justContext = justContext;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public List<UUID> getChildrenIds() {
        return childrenIds;
    }

    public void setChildrenIds(List<UUID> childrenIds) {
        this.childrenIds = childrenIds;
    }

    public UUID getDiffFileId() {
        return diffFileId;
    }

    public void setDiffFileId(UUID diffFileId) {
        this.diffFileId = diffFileId;
    }
}
