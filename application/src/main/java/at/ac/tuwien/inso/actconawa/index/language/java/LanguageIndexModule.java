package at.ac.tuwien.inso.actconawa.index.language.java;

import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffFile;

public interface LanguageIndexModule {

    boolean parseSemanticalDiff(GitCommitDiffFile commitDiffFile);

}
