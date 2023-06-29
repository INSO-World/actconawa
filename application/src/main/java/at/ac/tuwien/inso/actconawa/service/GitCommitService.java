package at.ac.tuwien.inso.actconawa.service;

import at.ac.tuwien.inso.actconawa.api.CommitService;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitRelationshipDto;
import at.ac.tuwien.inso.actconawa.exception.CommitNotFoundException;
import at.ac.tuwien.inso.actconawa.mapper.GitMapper;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRelationshipRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    public List<GitCommitDto> findAncestors(UUID gitCommitId, int maxDepth) {
        var result = new ArrayList<GitCommitDto>();
        var commit = gitCommitRepository.findById(gitCommitId)
                .orElseThrow(CommitNotFoundException::new);

        for (int depth = 0; depth <= maxDepth; depth++) {
            if (Optional.ofNullable(commit.getParents()).map(List::size).orElse(0) > 0) {
                result.add(gitMapper.mapModelToDto(commit));
                if (commit.getParents().size() > 1) {
                    //commit.getParents().forEach(x -> result.add(gitMapper.mapModelToDto(x.getParent())));
                    // return as the commit has multiple parents
                    return result;
                }
            }
            // No more parents means that there are no more commits to fetch
            if (CollectionUtils.isEmpty(commit.getParents())) {
                result.add(gitMapper.mapModelToDto(commit));
                return result;
            }
            // proceed with the next commit
            commit = commit.getParents().get(0).getParent();
        }
        return result;

    }


}
