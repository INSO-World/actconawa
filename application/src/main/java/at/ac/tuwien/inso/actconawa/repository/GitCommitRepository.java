package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitCommitRepository extends JpaRepository<GitCommit, Long> {
}
