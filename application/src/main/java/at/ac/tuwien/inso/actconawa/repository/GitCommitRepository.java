package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface GitCommitRepository extends JpaRepository<GitCommit, UUID> {

    @Query(value = """
                   WITH RECURSIVE linked_entries(id, parent_id, child_id, stop, depth) AS (
                     SELECT icr.id,
                            icr.parent_id ,
                            icr.child_id,
                            (SELECT count(c.child_id) FROM commit_relationship c WHERE c.child_id = icr.child_id) > 1,
                            0
                     FROM commit_relationship icr
                     WHERE icr.child_id = :childId
                     UNION
                     SELECT ou.id,
                            ou.parent_id,
                            ou.child_id,
                            (SELECT count(c.child_id) FROM commit_relationship c WHERE c.child_id = ou.child_id) > 1,
                            depth + 1
                     FROM linked_entries
                     INNER JOIN commit_relationship AS ou ON linked_entries.parent_id = ou.child_id
                     WHERE stop is false AND depth < :maxDepth
                   )
                   SELECT distinct comm.*
                   FROM linked_entries le JOIN commit comm ON comm.id = child_id
                   ORDER BY comm.max_distance_from_root DESC ;
            """, nativeQuery = true)
    List<GitCommit> findAncestors(@Param("childId") UUID childId, @Param("maxDepth") int maxDepth);

    @Query("select r.child from GitCommitRelationship r where r.parent is null ")
    List<GitCommit> findCommitsWithoutParents();

    @Query("select c from GitCommit c inner join GitBranch b on c.id = b.headCommit.id")
    Set<GitCommit> findBranchHeadCommits();

    Optional<GitCommit> findByShaStartsWith(String sha);


}
