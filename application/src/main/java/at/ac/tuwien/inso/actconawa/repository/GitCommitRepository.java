package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
                   FROM linked_entries le JOIN commit comm ON comm.id = child_id;
            """, nativeQuery = true)
    List<GitCommit> findAncestors(@Param("childId") UUID childId, @Param("maxDepth") int maxDepth);

    @Query("select r.child from GitCommitRelationship r where r.parent is null ")
    List<GitCommit> findCommitsWithoutParents();

    @Query("select r.child.id from GitCommitRelationship r where r.parent = :commit ")
    List<UUID> findChildCommitIdsOfCommit(GitCommit commit);

    @Query("select r.parent.id from GitCommitRelationship r where r.child = :commit ")
    List<UUID> findParentCommitIdsOfCommit(GitCommit commit);

    @Query(value = """
            select hdc.commit_id from commit_relationship
            join commit_diff_file cdf
                on commit_relationship.id = cdf.commit_relationship_id
                       and child_id = :commitId
            join commit_diff_hunk cdh on cdf.id = cdh.diff_file_id
            join hunk_dependency_commit hdc on cdh.id = hdc.hunk_id
            """, nativeQuery = true)
    List<UUID> findCommitDependencyCommitIds(UUID commitId);

    @Query(value = """
            select hdc.commit_id from commit_relationship
            join commit_diff_file cdf
                on commit_relationship.id = cdf.commit_relationship_id
                       and child_id = :commitId
            join commit_diff_hunk cdh on cdf.id = cdh.diff_file_id
            join hunk_dependency_commit hdc on cdh.id = hdc.hunk_id
            """, nativeQuery = true)
    List<byte[]> findCommitDependencyCommitIdsH2ByteArrayList(UUID commitId);


    default List<UUID> findCommitDependencyCommitIdsH2List(UUID commitId) {
        return findCommitDependencyCommitIdsH2ByteArrayList(commitId).stream()
                .map(UUID::nameUUIDFromBytes)
                .collect(Collectors.toList());
    }

    Optional<GitCommit> findByShaStartsWith(String sha);


}
