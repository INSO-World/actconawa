package at.ac.tuwien.inso.actconawa.api;

import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffFileDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffHunkDto;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;
import java.util.UUID;

public interface DiffService {

    /**
     * Retrieve a diff between a commit and a parent.
     *
     * @param commit       the {@link RevCommit}.
     * @param parentCommit parent {@link RevCommit}
     * @return the formatted diff.
     */
    String getDiff(RevCommit commit, RevCommit parentCommit);


    /**
     * Retrieve a diff of a root commit (without parent).
     *
     * @param commit the {@link RevCommit}.
     * @return the formatted diff.
     */
    String getDiff(RevCommit commit);

    /**
     * Retrieve all the diff hunks of a {@link GitCommitDiffFileDto}. In case no hunks are found for
     * a provided {@link UUID} then an empty list is returned.
     *
     * @param commitDiffFileId the id of the {@link GitCommitDiffFileDto}.
     * @return a {@link List} of {@link GitCommitDiffHunkDto}s.
     */
    List<GitCommitDiffHunkDto> findGitCommitDiffHunksByDiffFileId(UUID commitDiffFileId);
}
