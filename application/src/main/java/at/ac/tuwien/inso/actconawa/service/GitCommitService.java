package at.ac.tuwien.inso.actconawa.service;

import at.ac.tuwien.inso.actconawa.api.CommitService;
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
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    public String getDiff(UUID gitCommitId, UUID parentCommitId) {
        return getDiff(
                getRevCommitByGitCommitId(gitCommitId),
                getRevCommitByGitCommitId(parentCommitId)
        );
    }

    @Override
    public String getDiff(RevCommit gitCommit, RevCommit parentCommit) {
        try (var reader = git.getRepository().newObjectReader();
             var outputStream = new ByteArrayOutputStream();
             var formatter = new DiffFormatter(outputStream)
        ) {
            var commitTreeId = gitCommit.getTree().getId();
            var parentCommitTreeId = parentCommit.getTree().getId();

            var commitTree = new CanonicalTreeParser(null, reader, commitTreeId);
            var parentCommitTree = new CanonicalTreeParser(null, reader, parentCommitTreeId);

            formatter.setRepository(git.getRepository());
            formatter.format(parentCommitTree, commitTree);
            return outputStream.toString();
        } catch (IOException e) {
            LOG.error("Creating diff between {} and {} failed.",
                    gitCommit.getId().getName(),
                    parentCommit.getId().getName(),
                    e);
            return null;
        }
    }

    @Override
    public String getDiff(RevCommit gitCommit) {
        try (var reader = git.getRepository().newObjectReader();
             var outputStream = new ByteArrayOutputStream();
             var formatter = new DiffFormatter(outputStream)
        ) {
            var commitTreeId = gitCommit.getTree().getId();

            var commitTree = new CanonicalTreeParser(null, reader, commitTreeId);

            formatter.setRepository(git.getRepository());
            formatter.format(new EmptyTreeIterator(), commitTree);
            return outputStream.toString();
        } catch (IOException e) {
            LOG.error("Creating diff of commit {} failed.",
                    gitCommit.getId().getName(),
                    e);
            return null;
        }
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
