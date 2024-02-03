package at.ac.tuwien.inso.actconawa.service;

import at.ac.tuwien.inso.actconawa.api.BranchService;
import at.ac.tuwien.inso.actconawa.dto.GitBranchDto;
import at.ac.tuwien.inso.actconawa.dto.GitBranchTrackingStatusDto;
import at.ac.tuwien.inso.actconawa.exception.BranchNotFoundException;
import at.ac.tuwien.inso.actconawa.exception.NoCommonMergeBaseException;
import at.ac.tuwien.inso.actconawa.mapper.GitMapper;
import at.ac.tuwien.inso.actconawa.repository.GitBranchRepository;
import at.ac.tuwien.inso.actconawa.repository.GitBranchTrackingStatusRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class GitBranchService implements BranchService {

    private final GitBranchRepository gitBranchRepository;

    private final GitBranchTrackingStatusRepository gitBranchTrackingStatusRepository;

    private final GitMapper gitMapper;

    public GitBranchService(GitBranchRepository gitBranchRepository, GitBranchTrackingStatusRepository gitBranchTrackingStatusRepository, GitMapper gitMapper) {
        this.gitBranchRepository = gitBranchRepository;
        this.gitBranchTrackingStatusRepository = gitBranchTrackingStatusRepository;
        this.gitMapper = gitMapper;
    }

    @Override
    public Page<GitBranchDto> findAll(Pageable pageable) {
        return gitBranchRepository.findAll(pageable).map(gitMapper::mapModelToDto);
    }

    public GitBranchTrackingStatusDto getBranchTrackingStatus(UUID gitBranchAId, UUID gitBranchBId) {
        if (gitBranchAId == null || gitBranchBId == null) {
            throw new IllegalArgumentException("Both branch ids must be provided and null values are not allowed");
        }
        var branchA = gitBranchRepository
                .findById(gitBranchAId).orElseThrow(BranchNotFoundException::new);
        var branchB = gitBranchRepository
                .findById(gitBranchBId).orElseThrow(BranchNotFoundException::new);
        var result = gitBranchTrackingStatusRepository.findOneByBranchAAndBranchB(branchA, branchB)
                .orElseThrow(NoCommonMergeBaseException::new);
        return gitMapper.mapModelToDto(result);
    }

}
