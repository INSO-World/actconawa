package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitBranch;
import at.ac.tuwien.inso.actconawa.persistence.GitBranchTrackingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GitBranchTrackingStatusRepository extends JpaRepository<GitBranchTrackingStatus, UUID> {

    Optional<GitBranchTrackingStatus> findOneByBranchAAndBranchB(GitBranch a, GitBranch b);
}
