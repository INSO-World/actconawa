package at.ac.tuwien.inso.actconawa.api;

import at.ac.tuwien.inso.actconawa.dto.GitCommitBranchRelationshipDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffFileDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitGroupDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitRelationshipDto;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CommitService {

    /**
     * Return all stored commits (paginated).
     *
     * @param pageable The pagination information.
     * @return A page containing {@link GitCommitDto}s.
     */
    Page<GitCommitDto> findAll(Pageable pageable);

    /**
     * Return all commit-commit relationships (paginated).
     *
     * @param pageable The pagination information.
     * @return A page containing {@link GitCommitRelationshipDto}
     */
    Page<GitCommitRelationshipDto> findAllRelations(Pageable pageable);

    /**
     * Return all commit groups.
     *
     * @param pageable The pagination information.
     * @return A page containing {@link GitCommitGroupDto}
     */
    Page<GitCommitGroupDto> findAllCommitGroups(Pageable pageable);

    /**
     * Return all Branches in which the commit occurs in.
     *
     * @param gitCommitId ID of the commit to find branches for.
     * @return {@link GitCommitBranchRelationshipDto}
     */
    GitCommitBranchRelationshipDto findBranches(UUID gitCommitId);

    /**
     * Find all files modified between a commit and it's parent.
     *
     * @param gitCommitId    The commit id to check for
     * @param parentCommitId The parent commit id to check for
     * @return a list of {@link GitCommitDiffFileDto}
     */
    List<GitCommitDiffFileDto> findModifiedFiles(UUID gitCommitId, UUID parentCommitId);

    /**
     * Takes a GitCommit id and returns ancestors. Ancestors are returned to a depth of maxDepth or less if a
     * multi-parent commit is reached. In such a case, the multi parent commit is returned, however the lookup will not
     * go further into depth. The commits are returned in topological order (leaf to root).
     *
     * @param gitCommitId The id of the commit to search ancestors commits for
     * @param maxDepth    The maximum amount of commits to be returned
     * @return A List of ancestor commits.
     */
    List<GitCommitDto> findAncestors(UUID gitCommitId, int maxDepth);

    // TODO: Move this into a internal service.
    RevCommit getRevCommitByGitCommitId(UUID commitId);

}
