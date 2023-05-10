package at.ac.tuwien.inso.actconawa.api;

import at.ac.tuwien.inso.actconawa.dto.GitBranchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BranchService {
    Page<GitBranchDto> findAll(Pageable pageable);
}
