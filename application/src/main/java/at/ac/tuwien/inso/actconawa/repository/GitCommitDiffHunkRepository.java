package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffHunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GitCommitDiffHunkRepository extends JpaRepository<GitCommitDiffHunk, UUID> {

}
