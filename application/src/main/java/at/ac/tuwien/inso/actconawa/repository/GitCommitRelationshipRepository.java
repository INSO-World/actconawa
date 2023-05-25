package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitCommitRelationship;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitRelationshipKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitCommitRelationshipRepository
        extends JpaRepository<GitCommitRelationship, GitCommitRelationshipKey> {

}
