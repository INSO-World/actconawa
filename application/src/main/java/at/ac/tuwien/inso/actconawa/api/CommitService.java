package at.ac.tuwien.inso.actconawa.api;

import at.ac.tuwien.inso.actconawa.dto.GitCommitBranchRelationshipDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffFileDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitRelationshipDto;
import at.ac.tuwien.inso.actconawa.exception.CommitNotFoundException;
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
     * go further into depth.
     *
     * @param gitCommitId The id of the commit to search ancestors commits for
     * @param maxDepth    The maximum amount of commits to be returned
     * @return A List of ancestor commits.
     */
    List<GitCommitDto> findAncestors(UUID gitCommitId, int maxDepth);

    /**
     * Takes two commits and returns the lowest common ancestor commit.
     *
     * Only branch head commits are supported.
     *
     * @param gitCommitAId Id of the first commit
     * @param gitCommitBId Id of the secondCommit
     * @throws IllegalArgumentException in case one of the commit IDs is not a branch head.
     * @throws IllegalStateException    in case the LCA commit is not found even that the provided commits were branch
     *                                  heads.
     * @throws CommitNotFoundException  in case one of the commits was not found.
     */
    GitCommitDto findLowestCommonAncestor(UUID gitCommitAId, UUID gitCommitBId);

    // TODO: Move this into a internal service.
    RevCommit getRevCommitByGitCommitId(UUID commitId);

}
