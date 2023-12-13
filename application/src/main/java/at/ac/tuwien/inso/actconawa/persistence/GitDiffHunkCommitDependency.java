package at.ac.tuwien.inso.actconawa.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "hunk_commit_dependency", uniqueConstraints = {
        @UniqueConstraint(
                name = "hunk_commit_dependency_hunk_commit_key",
                columnNames = {"hunk_id", "commit_id"}
        )
})
public class GitDiffHunkCommitDependency implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "hunk_id")
    private GitCommitDiffHunk hunk;

    @ManyToOne(optional = false)
    @JoinColumn(name = "commit_id")
    private GitCommit commit;

    public GitDiffHunkCommitDependency() {
    }

    public GitDiffHunkCommitDependency(GitCommitDiffHunk hunk, GitCommit commit) {
        this.hunk = hunk;
        this.commit = commit;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public GitCommitDiffHunk getHunk() {
        return hunk;
    }

    public void setHunk(GitCommitDiffHunk hunk) {
        this.hunk = hunk;
    }

    public GitCommit getCommit() {
        return commit;
    }

    public void setCommit(GitCommit commit) {
        commit = commit;
    }
}
