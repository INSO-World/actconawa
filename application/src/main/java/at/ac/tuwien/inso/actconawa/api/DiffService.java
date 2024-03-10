package at.ac.tuwien.inso.actconawa.api;

import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffCodeChangeDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffFileDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffHunkDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffLineChangeDto;
import at.ac.tuwien.inso.actconawa.dto.GitPatchDto;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;
import java.util.UUID;

public interface DiffService {

    /**
     * Retrieve a diff between a commit and a parent.
     *
     * @param commit         the {@link RevCommit}.
     * @param parentCommit   parent {@link RevCommit}. This value might be null for root commits without parent.
     * @param contextLines   the number of context lines to be added to the changed lines
     *                       changes (0 == only changed lines themselves)
     * @return the formatted diff.
     */
    String getDiff(RevCommit commit, RevCommit parentCommit, int contextLines);

    /**
     * Retrieve a diff between a commit and a parent.
     *
     * @param commitId       the id of the commit {@link UUID}.
     * @param parentCommitId parent commit id ({@link UUID}). This value might be null for root commits without parent.
     * @param contextLines   the number of context lines to be added to the changed lines changes (0 == only changed
     *                       lines themselves)
     * @return the formatted diff.
     */
    GitPatchDto getDiff(UUID commitId, UUID parentCommitId, int contextLines);

    /**
     * Retrieve all the diff hunks of a {@link GitCommitDiffFileDto}. In case no hunks are found for
     * a provided {@link UUID} then an empty list is returned.
     *
     * @param commitDiffFileId the id of the {@link GitCommitDiffFileDto}.
     * @return a {@link List} of {@link GitCommitDiffHunkDto}s.
     */
    List<GitCommitDiffHunkDto> findGitCommitDiffHunksByDiffFileId(UUID commitDiffFileId);

    /**
     * Retrieve all the changed lines of a {@link GitCommitDiffFileDto}. In case no changes are found for a provided
     * {@link UUID} then an empty list is returned. In opposite of {@link #findGitCommitDiffHunksByDiffFileId(UUID)}
     * which returns hunks with context as in a patch, this method returns only the actually changed lines, without
     * context.
     *
     * @param commitDiffFileId the id of the {@link GitCommitDiffFileDto}.
     * @return a {@link List} of {@link GitCommitDiffLineChangeDto}s.
     */
    List<GitCommitDiffLineChangeDto> findGitCommitLineChangesByDiffFileId(UUID commitDiffFileId);

    /**
     * Retrieve all the indexed code changes of a {@link GitCommitDiffFileDto}. In case no changes are found for a
     * provided {@link UUID} then an empty list is returned. There is no guarantee for completeness. Results depends on
     * the specific code indexer. Further, renames of language specific structures or moving  structures to other files
     * is hard to impossible to capture correctly. Thus this information is nowhere indexed.
     *
     * @param commitDiffFileId the id of the {@link GitCommitDiffFileDto}.
     * @return a {@link List} of {@link GitCommitDiffLineChangeDto}s.
     */
    List<GitCommitDiffCodeChangeDto> findGitCommitCodeChangesByDiffFileId(UUID commitDiffFileId);
}
