package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitCommitLowestCommonAncestor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GitCommitLowestCommonAncestorRepository extends JpaRepository<GitCommitLowestCommonAncestor, UUID> {

}
