package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GitCommitRepository extends JpaRepository<GitCommit, UUID> {

    @Query(value = "WITH RECURSIVE linked_entries(id, parent_id, child_id, stop, depth) AS (\n"
            + "  SELECT icr.id, icr.parent_id  , icr.child_id, (select count(c.child_id) from commit_relationship c where c.child_id = icr.child_id) > 1, 0\n"
            + "  FROM commit_relationship icr\n"
            + "  WHERE icr.child_id = :childId\n"
            + "  UNION\n"
            + "  SELECT ou.id, ou.parent_id, ou.child_id, (select count(c.child_id) from commit_relationship c where c.child_id = ou.child_id) > 1, depth + 1\n"
            + "  FROM linked_entries\n"
            + "  INNER JOIN commit_relationship AS ou ON linked_entries.parent_id = ou.child_id\n"
            + "  WHERE stop is false and depth < :maxDepth"
            + ")\n"
            + "SELECT distinct comm.* FROM linked_entries le join commit comm on comm.id = child_id order by comm.max_distance_from_root desc ;", nativeQuery = true)
    List<GitCommit> findAncestors(@Param("childId") UUID childId, @Param("maxDepth") int maxDepth);

    @Query("select r.child from GitCommitRelationship r where r.parent is null ")
    List<GitCommit> findCommitsWithoutParents();
}
