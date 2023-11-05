package at.ac.tuwien.inso.actconawa.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.eclipse.jgit.diff.DiffEntry;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "commit_diff_file")
public class GitCommitDiffFile implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(referencedColumnName = "parent_id"),
            @JoinColumn(referencedColumnName = "child_id")
    })
    private GitCommitRelationship commitRelationship;

    @Column(nullable = false)
    private String newFilePath;

    @Column
    private String oldFilePath;

    @Enumerated(value = EnumType.STRING)
    private DiffEntry.ChangeType changeType;

    @OneToMany(mappedBy = "diffFile")
    private List<GitCommitDiffHunk> gitCommitDiffHunks;

    public GitCommitDiffFile() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public GitCommitRelationship getCommitRelationship() {
        return commitRelationship;
    }

    public void setCommitRelationship(GitCommitRelationship commitRelationship) {
        this.commitRelationship = commitRelationship;
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

    public DiffEntry.ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(DiffEntry.ChangeType changeType) {
        this.changeType = changeType;
    }

    public List<GitCommitDiffHunk> getGitCommitDiffHunks() {
        return gitCommitDiffHunks;
    }

    public void setGitCommitDiffHunks(List<GitCommitDiffHunk> gitCommitDiffHunks) {
        this.gitCommitDiffHunks = gitCommitDiffHunks;
    }
}
