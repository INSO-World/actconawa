package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface GitCommitDiffFileRepository extends JpaRepository<GitCommitDiffFile, UUID> {

    @Query("select d from GitCommitDiffFile d where"
            + " d.commitRelationship.child.id = :commitId and"
            + " d.commitRelationship.parent.id = :parentId"
    )
    List<GitCommitDiffFile> findByCommitAndParent(UUID commitId, UUID parentId);

}
