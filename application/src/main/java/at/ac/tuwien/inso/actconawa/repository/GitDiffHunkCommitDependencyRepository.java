package at.ac.tuwien.inso.actconawa.repository;

import at.ac.tuwien.inso.actconawa.persistence.GitDiffHunkCommitDependency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GitDiffHunkCommitDependencyRepository extends JpaRepository<GitDiffHunkCommitDependency, UUID> {


}
