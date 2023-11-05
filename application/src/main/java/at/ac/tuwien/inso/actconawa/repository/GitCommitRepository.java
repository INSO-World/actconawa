package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GitCommitRepository extends JpaRepository<GitCommit, UUID> {
}
