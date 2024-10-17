package at.ac.tuwien.inso.actconawa.dto;

import java.io.Serializable;
import java.util.UUID;


public class GitCommitGroupDto implements Serializable {

    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
