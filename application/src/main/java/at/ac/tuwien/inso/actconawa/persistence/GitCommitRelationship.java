package at.ac.tuwien.inso.actconawa.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.springframework.context.annotation.Lazy;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "commit_relationship", uniqueConstraints = {
        @UniqueConstraint(
                name = "commit_relationship_parent_child_key",
                columnNames = {"parent_id", "child_id"}
        )
})
public class GitCommitRelationship implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private GitCommit parent;

    @ManyToOne(optional = false)
    private GitCommit child;

    @Lazy
    @OneToMany(mappedBy = "commitRelationship")
    private List<GitCommitDiffFile> affectedFiles;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

}
