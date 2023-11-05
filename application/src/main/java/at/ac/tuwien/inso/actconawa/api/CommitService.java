package at.ac.tuwien.inso.actconawa.api;

import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffFileDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitRelationshipDto;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CommitService {

    Page<GitCommitDto> findAll(Pageable pageable);

    Page<GitCommitRelationshipDto> findAllRelations(Pageable pageable);

    List<GitCommitDiffFileDto> findModifiedFiles(UUID gitCommitId, UUID parentCommitId);

    RevCommit getRevCommitByGitCommitId(UUID commitId);

    /**
     * Takes a GitCommit id and returns ancestors. Ancestors are returned to a depth of maxDepth or
     * less if a multi-parent commit is reached. In such a case, the multi parent commit is
     * returned, however the lookup will not go further into depth.
     *
     * @param gitCommitId The id of the commit to search ancestors commits for
     * @param maxDepth    The maximum amount of commits to be returned
     * @return A List of ancestor commits.
     */
    List<GitCommitDto> findAncestors(UUID gitCommitId, int maxDepth);

}
