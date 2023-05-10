package at.ac.tuwien.inso.actconawa.api;

import at.ac.tuwien.inso.actconawa.dto.GitCommitDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommitService {
    Page<GitCommitDto> findAll(Pageable pageable);
}
