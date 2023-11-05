package at.ac.tuwien.inso.actconawa.api;

import org.eclipse.jgit.revwalk.RevCommit;

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
}
