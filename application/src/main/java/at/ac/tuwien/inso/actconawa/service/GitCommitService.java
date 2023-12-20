package at.ac.tuwien.inso.actconawa.service;

import at.ac.tuwien.inso.actconawa.api.CommitService;
import at.ac.tuwien.inso.actconawa.dto.GitCommitBranchRelationshipDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffFileDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitRelationshipDto;
import at.ac.tuwien.inso.actconawa.exception.CommitNotFoundException;
import at.ac.tuwien.inso.actconawa.mapper.GitMapper;
import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import at.ac.tuwien.inso.actconawa.repository.GitCommitDiffFileRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRelationshipRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRepository;
import jakarta.transaction.Transactional;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class GitCommitService implements CommitService {

    private static final Logger LOG = LoggerFactory.getLogger(GitCommitService.class);

    private final GitCommitRepository gitCommitRepository;

    private final GitCommitRelationshipRepository gitCommitRelationshipRepository;

    private final GitCommitDiffFileRepository gitCommitDiffFileRepository;

    private final GitMapper gitMapper;

    private final Git git;


    public GitCommitService(GitCommitRepository gitCommitRepository,
            GitCommitRelationshipRepository gitCommitRelationshipRepository,
            GitCommitDiffFileRepository gitCommitDiffFileRepository, GitMapper gitMapper, Git git) {
        this.gitCommitRepository = gitCommitRepository;
        this.gitCommitRelationshipRepository = gitCommitRelationshipRepository;
        this.gitCommitDiffFileRepository = gitCommitDiffFileRepository;
        this.gitMapper = gitMapper;
        this.git = git;
    }

    @Override
    public Page<GitCommitDto> findAll(Pageable pageable) {
        return gitCommitRepository.findAll(pageable).map(gitMapper::mapModelToDto);
    }

    @Override
    public List<GitCommitDiffFileDto> findModifiedFiles(UUID gitCommitId, UUID parentCommitId) {
        return gitCommitDiffFileRepository.findByCommitAndParent(gitCommitId, parentCommitId)
                .stream().map(gitMapper::mapModelToDto)
                .collect(Collectors.toList());
    }

    @Override
    public RevCommit getRevCommitByGitCommitId(UUID commitId) {
        if (commitId == null) {
            throw new IllegalArgumentException("Commit Id must not be null");
        }
        var repo = git.getRepository();
        try {
            return repo.parseCommit(repo.resolve(gitCommitRepository.findById(commitId)
                    .map(GitCommit::getSha)
                    .orElseThrow(CommitNotFoundException::new)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<GitCommitRelationshipDto> findAllRelations(Pageable pageable) {
        return gitCommitRelationshipRepository.findAll(pageable).map(gitMapper::mapModelToDto);
    }

    @Override
    public GitCommitBranchRelationshipDto findBranches(UUID gitCommitId) {
        var commit = gitCommitRepository.findById(gitCommitId).orElseThrow(CommitNotFoundException::new);
        return gitMapper.mapModelToBranchRelationshipDto(commit);
    }

    @Override
    public List<GitCommitDto> findAncestors(UUID gitCommitId, int maxDepth) {
        LOG.debug("Returning ancestors for Commit with ID {} and max depth {}",
                gitCommitId,
                maxDepth);
        return gitCommitRepository.findAncestors(gitCommitId, maxDepth).stream().map(gitMapper::mapModelToDto).toList();
    }
}
