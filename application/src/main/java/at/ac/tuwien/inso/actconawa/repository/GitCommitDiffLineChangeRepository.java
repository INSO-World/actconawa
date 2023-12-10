package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffLineChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface GitCommitDiffLineChangeRepository extends JpaRepository<GitCommitDiffLineChange, UUID> {

    @Query("select h from GitCommitDiffLineChange h where h.diffFile.id = :commitDiffId")
    List<GitCommitDiffLineChange> findByCommitDiffFile(UUID commitDiffId);

}
