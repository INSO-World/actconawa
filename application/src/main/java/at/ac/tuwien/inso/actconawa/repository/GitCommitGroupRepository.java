package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitCommitGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GitCommitGroupRepository extends JpaRepository<GitCommitGroup, UUID> {

}
