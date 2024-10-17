package at.ac.tuwien.inso.actconawa.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "commit_group")
public class GitCommitGroup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(mappedBy = "group", cascade = CascadeType.PERSIST)
    private List<GitCommit> commits;

    public GitCommitGroup() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<GitCommit> getCommits() {
        return commits;
    }

    public void setCommits(List<GitCommit> commits) {
        this.commits = commits;
    }
}
