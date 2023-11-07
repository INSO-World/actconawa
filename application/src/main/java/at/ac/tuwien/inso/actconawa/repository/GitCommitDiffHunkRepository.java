package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffHunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface GitCommitDiffHunkRepository extends JpaRepository<GitCommitDiffHunk, UUID> {

    @Query("select h from GitCommitDiffHunk h where h.diffFile.id = :commitDiffId")
    List<GitCommitDiffHunk> findByCommitDiffFile(UUID commitDiffId);

}
