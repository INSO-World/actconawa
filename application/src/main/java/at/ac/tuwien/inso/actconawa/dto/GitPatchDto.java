package at.ac.tuwien.inso.actconawa.dto;

import java.io.Serializable;

public class GitPatchDto implements Serializable {

    private String patch;

    public String getPatch() {
        return patch;
    }

    public void setPatch(String patch) {
        this.patch = patch;
    }
}
