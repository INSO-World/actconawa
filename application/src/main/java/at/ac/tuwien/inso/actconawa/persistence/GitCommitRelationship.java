package at.ac.tuwien.inso.actconawa.persistence;

import jakarta.persistence.Basic;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.springframework.context.annotation.Lazy;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "commit_relationship")
public class GitCommitRelationship implements Serializable {

    @EmbeddedId
    private GitCommitRelationshipKey id;

    @ManyToOne
    @MapsId("parent_id")
    private GitCommit parent;

    @ManyToOne
    @MapsId("child_id")
    private GitCommit child;

    @Lazy
    @OneToMany(mappedBy = "commitRelationship")
    private List<GitCommitDiffFile> affectedFiles;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String diff;

    public GitCommitRelationshipKey getId() {
        return id;
    }

    public void setId(GitCommitRelationshipKey id) {
        this.id = id;
    }

    public GitCommit getParent() {
        return parent;
    }

    public void setParent(GitCommit parent) {
        this.parent = parent;
    }

    public GitCommit getChild() {
        return child;
    }

    public void setChild(GitCommit child) {
        this.child = child;
    }

    public List<GitCommitDiffFile> getAffectedFiles() {
        return affectedFiles;
    }

    public void setAffectedFiles(List<GitCommitDiffFile> affectedFiles) {
        this.affectedFiles = affectedFiles;
    }

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }
}
