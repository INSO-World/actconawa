package at.ac.tuwien.inso.actconawa.service;

import at.ac.tuwien.inso.actconawa.index.Indexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class IndexerService {

    private static final Logger LOG = LoggerFactory.getLogger(IndexerService.class);


    private final List<Indexer> indexers;

    public IndexerService(List<Indexer> indexers) {
        this.indexers = indexers;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void indexAll() {
        var overallIndexStart = Instant.now();
        for (Indexer indexer : indexers) {
            var indexerStart = Instant.now();
            LOG.info("Starting indexing of {}", indexer.getIndexedContentDescription());
            indexer.index();
            var finished = Instant.now();
            LOG.info("Finished indexing of {} in {}ms",
                    indexer.getIndexedContentDescription(),
                    Duration.between(indexerStart, finished).toMillis());
        }
        LOG.info("Completed indexing in {}ms", Duration.between(overallIndexStart, Instant.now()).toMillis());
    }
}
