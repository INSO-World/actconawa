package at.ac.tuwien.inso.actconawa.service;

import at.ac.tuwien.inso.actconawa.api.CommitService;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitRelationshipDto;
import at.ac.tuwien.inso.actconawa.mapper.GitMapper;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRelationshipRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class GitCommitService implements CommitService {

    private final GitCommitRepository gitCommitRepository;

    private final GitCommitRelationshipRepository gitCommitRelationshipRepository;

    private final GitMapper gitMapper;

    public GitCommitService(GitCommitRepository gitCommitRepository,
            GitCommitRelationshipRepository gitCommitRelationshipRepository,
            GitMapper gitMapper) {
        this.gitCommitRepository = gitCommitRepository;
        this.gitCommitRelationshipRepository = gitCommitRelationshipRepository;
        this.gitMapper = gitMapper;
    }

    @Override
    public Page<GitCommitDto> findAll(Pageable pageable) {
        return gitCommitRepository.findAll(pageable).map(gitMapper::mapModelToDto);
    }

    @Override
    public Page<GitCommitRelationshipDto> findAllRelations(Pageable pageable) {
        return gitCommitRelationshipRepository.findAll(pageable).map(gitMapper::mapModelToDto);
    }
}
