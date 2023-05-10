package at.ac.tuwien.inso.actconawa.service;

import at.ac.tuwien.inso.actconawa.api.CommitService;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDto;
import at.ac.tuwien.inso.actconawa.mapper.GitMapper;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class GitCommitService implements CommitService {

    private final GitCommitRepository gitCommitRepository;

    private final GitMapper gitMapper;

    public GitCommitService(GitCommitRepository gitCommitRepository, GitMapper gitMapper) {
        this.gitCommitRepository = gitCommitRepository;
        this.gitMapper = gitMapper;
    }

    @Override
    public Page<GitCommitDto> findAll(Pageable pageable) {
        return gitMapper.mapCommitPage(gitCommitRepository.findAll(pageable));
    }
}
