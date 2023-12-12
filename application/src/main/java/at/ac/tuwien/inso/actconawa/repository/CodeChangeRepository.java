package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.CodeChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CodeChangeRepository extends JpaRepository<CodeChange, UUID> {

    @Query("select c from CodeChange c where c.diffFile = :commitDiffId")
    List<CodeChange> findByCommitDiffFile(UUID commitDiffId);

}
