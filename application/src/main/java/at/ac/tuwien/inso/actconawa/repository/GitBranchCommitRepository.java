package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitBranchCommit;
import at.ac.tuwien.inso.actconawa.persistence.GitBranchCommitKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitBranchCommitRepository
        extends JpaRepository<GitBranchCommit, GitBranchCommitKey> {

}
