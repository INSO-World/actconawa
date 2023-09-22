package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GitCommitDiffFileRepository extends JpaRepository<GitCommitDiffFile, UUID> {

}
