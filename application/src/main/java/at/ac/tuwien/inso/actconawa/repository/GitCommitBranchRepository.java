package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitCommitBranch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GitCommitBranchRepository extends JpaRepository<GitCommitBranch, UUID> {


}
