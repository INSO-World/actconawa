package at.ac.tuwien.inso.actconawa.api;

import at.ac.tuwien.inso.actconawa.dto.GitBranchDto;
import at.ac.tuwien.inso.actconawa.dto.GitBranchTrackingStatusDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BranchService {

    /**
     * Return all branches (paginated).
     *
     * @param pageable The page request.
     * @return a Page of {@link GitBranchDto}s.
     */
    Page<GitBranchDto> findAll(Pageable pageable);

    /**
     * Returns the tracking status between two branches. {@link GitBranchTrackingStatusDto} contains the merge base
     * commit id and the ahead/behind count. The order matters for the ahead/behind count, gitBranchAId is used as
     * reference.
     *
     * @return The requested {@link GitBranchTrackingStatusDto}.
     */
    GitBranchTrackingStatusDto getBranchTrackingStatus(UUID gitBranchAId, UUID gitBranchBId);

    /**
     * Returns all (paginated) tracking status between branches. {@link GitBranchTrackingStatusDto} contains the merge
     * base commit id and the ahead/behind count. The order matters for the ahead/behind count, gitBranchAId is used as
     * reference.
     *
     * @return The requested {@link Page} with the {@link GitBranchTrackingStatusDto}s.
     */
    Page<GitBranchTrackingStatusDto> getAllBranchTrackingStatus(Pageable pageable);
}
