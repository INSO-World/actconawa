package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitCommitRelationship;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitRelationshipKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GitCommitRelationshipRepository
        extends JpaRepository<GitCommitRelationship, GitCommitRelationshipKey> {

    @Query(value = "WITH RECURSIVE linked_entries(parent_id, child_id) AS (\n"
            + "  SELECT parent_id  , child_id\n"
            + "  FROM commit_relationship\n"
            + "  WHERE child_id = :childId\n"
            + "  UNION ALL\n"
            + "  SELECT ou.parent_id, ou.child_id\n"
            + "  FROM linked_entries\n"
            + "  INNER JOIN commit_relationship AS ou ON linked_entries.parent_id = ou.child_id\n"
            + ")\n"
            + "SELECT parent_id,child_id\n"
            + "FROM linked_entries GROUP BY parent_id,child_id;", nativeQuery = true)
    List<GitCommitRelationship> findAncestors(@Param("childId") UUID childId);
}
