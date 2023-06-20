package at.ac.tuwien.inso.actconawa.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "branch")
public class GitBranch implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "head_commit_id")
    private GitCommit headCommit;

    private boolean remoteHead;

    private boolean containingExclusiveCommits;

    public GitBranch() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GitCommit getHeadCommit() {
        return headCommit;
    }

    public void setHeadCommit(GitCommit headCommit) {
        this.headCommit = headCommit;
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
