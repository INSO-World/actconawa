package at.ac.tuwien.inso.actconawa.api;

import at.ac.tuwien.inso.actconawa.dto.GitCommitDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitRelationshipDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommitService {

    Page<GitCommitDto> findAll(Pageable pageable);

    Page<GitCommitRelationshipDto> findAllRelations(Pageable pageable);

}
