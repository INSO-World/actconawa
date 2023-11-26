package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitBranch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GitBranchRepository extends JpaRepository<GitBranch, UUID> {

}
