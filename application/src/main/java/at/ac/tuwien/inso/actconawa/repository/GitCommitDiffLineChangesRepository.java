package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffLineChanges;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface GitCommitDiffLineChangesRepository extends JpaRepository<GitCommitDiffLineChanges, UUID> {

    @Query("select h from GitCommitDiffLineChanges h where h.diffFile.id = :commitDiffId")
    List<GitCommitDiffLineChanges> findByCommitDiffFile(UUID commitDiffId);

}
