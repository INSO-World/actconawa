package at.ac.tuwien.inso.actconawa.index;

import at.ac.tuwien.inso.actconawa.index.language.LanguageIndexModule;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffFile;
import at.ac.tuwien.inso.actconawa.repository.GitCommitDiffFileRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(4)
public class SemanticalDiffIndexer implements Indexer {

    private static final Logger LOG = LoggerFactory.getLogger(SemanticalDiffIndexer.class);

    private final GitCommitDiffFileRepository gitCommitDiffFileRepository;

    private final List<LanguageIndexModule> languageIndexModules;

    public SemanticalDiffIndexer(GitCommitDiffFileRepository gitCommitDiffFileRepository, List<LanguageIndexModule> languageIndexModules) {
        this.gitCommitDiffFileRepository = gitCommitDiffFileRepository;
        this.languageIndexModules = languageIndexModules;
    }

    @Transactional
    public void index() {
        for (GitCommitDiffFile commitDiffFile : this.gitCommitDiffFileRepository.findAll()) {
            for (LanguageIndexModule languageIndexModule : languageIndexModules) {
                if (languageIndexModule.parseSemanticalDiff(commitDiffFile)) {
                    break;
                }
            }
        }
    }

    @Override
    public String getIndexedContentDescription() {
        return "semantic diff information";
    }
}
