package at.ac.tuwien.inso.actconawa.service;

import at.ac.tuwien.inso.actconawa.api.BranchService;
import at.ac.tuwien.inso.actconawa.dto.GitBranchDto;
import at.ac.tuwien.inso.actconawa.mapper.GitMapper;
import at.ac.tuwien.inso.actconawa.repository.GitBranchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class GitBranchService implements BranchService {

    private final GitBranchRepository gitBranchRepository;

    private final GitMapper gitMapper;

    public GitBranchService(GitBranchRepository gitBranchRepository, GitMapper gitMapper) {
        this.gitBranchRepository = gitBranchRepository;
        this.gitMapper = gitMapper;
    }

    @Override
    public Page<GitBranchDto> findAll(Pageable pageable) {
        return gitBranchRepository.findAll(pageable).map(gitMapper::mapModelToDto);
    }
}
