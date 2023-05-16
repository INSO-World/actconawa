package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitCommitRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitCommitRelationshipRepository
        extends JpaRepository<GitCommitRelationship, Long> {

}
