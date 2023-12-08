package at.ac.tuwien.inso.actconawa.index;

import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffFile;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffHunk;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffLineChanges;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitRelationship;
import at.ac.tuwien.inso.actconawa.repository.GitCommitDiffFileRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitDiffHunkRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitDiffLineChangesRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitRelationshipRepository;
import at.ac.tuwien.inso.actconawa.service.GitCommitService;
import at.ac.tuwien.inso.actconawa.service.GitDiffService;
import jakarta.annotation.Nullable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.patch.Patch;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Component
@Order(2)
public class GitDiffIndexer implements Indexer {

    private static final Logger LOG = LoggerFactory.getLogger(GitDiffIndexer.class);

    private final Git git;

    private final GitCommitService gitCommitService;

    private final GitDiffService gitDiffService;

    private final GitCommitDiffHunkRepository gitDiffHunkRepository;

    private final GitCommitDiffLineChangesRepository gitCommitDiffLineChangesRepository;

    private final GitCommitDiffFileRepository gitCommitDiffFileRepository;

    private final GitCommitRelationshipRepository gitCommitRelationshipRepository;

    public GitDiffIndexer(Git git, GitCommitService gitCommitService, GitDiffService gitDiffService, GitCommitDiffHunkRepository gitDiffHunkRepository, GitCommitDiffLineChangesRepository gitCommitDiffLineChangesRepository, GitCommitDiffFileRepository gitCommitDiffFileRepository, GitCommitRelationshipRepository gitCommitRelationshipRepository) {
        this.git = git;
        this.gitCommitService = gitCommitService;
        this.gitDiffService = gitDiffService;
        this.gitDiffHunkRepository = gitDiffHunkRepository;
        this.gitCommitDiffLineChangesRepository = gitCommitDiffLineChangesRepository;
        this.gitCommitDiffFileRepository = gitCommitDiffFileRepository;
        this.gitCommitRelationshipRepository = gitCommitRelationshipRepository;

    }

    @Transactional
    public void index() {
        var commitDiffFiles = new ArrayList<GitCommitDiffFile>();
        var commitCache = new HashMap<String, GitCommit>();
        // Generic commits
        gitCommitRelationshipRepository.findAll().forEach(cr -> {
            commitCache.putIfAbsent(cr.getChild().getSha(), cr.getChild());
            if (cr.getParent() != null) {
                commitCache.putIfAbsent(cr.getParent().getSha(), cr.getParent());
            }
            commitDiffFiles.addAll(processDiffData(cr));
        });
        // TODO: Empty commits?

        gitCommitDiffLineChangesRepository.saveAll(
                commitDiffFiles.stream()
                        .map(GitCommitDiffFile::getGitCommitDiffLineChanges)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
        gitDiffHunkRepository.saveAll(
                commitDiffFiles.stream()
                        .map(GitCommitDiffFile::getGitCommitDiffHunks)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .map(h -> {
                            if (!CollectionUtils.isEmpty(h.getDependencyCommitShaSet())) {
                                h.setDependencies(new ArrayList<>());
                                h.getDependencyCommitShaSet()
                                        .forEach(x -> h.getDependencies().add(commitCache.get(x)));
                            }
                            return h;
                        })
                        .collect(Collectors.toList()));

        gitCommitDiffFileRepository.saveAll(commitDiffFiles);
    }

    @Override
    public String getIndexedContentDescription() {
        return "git diff and hunk dependency information";
    }

    private List<GitCommitDiffFile> processDiffData(GitCommitRelationship gitCommitRelationship) {
        var commit = gitCommitService
                .getRevCommitByGitCommitId(gitCommitRelationship.getChild().getId());
        if (gitCommitRelationship.getParent() != null) {
            var parentCommit = gitCommitService
                    .getRevCommitByGitCommitId(gitCommitRelationship.getParent().getId());
            return processDiffData(commit, parentCommit, gitCommitRelationship);
        } else {
            return processDiffData(commit, null, gitCommitRelationship);
        }
    }

    private List<GitCommitDiffFile> processDiffData(
            RevCommit commit,
            @Nullable RevCommit parentCommit,
            @Nullable GitCommitRelationship gitCommitRelationship
    ) {
        try (var gitObjectReader = git.getRepository().newObjectReader()) {
            var p = new Patch();
            if (parentCommit != null) {
                p.parse(new ByteArrayInputStream(gitDiffService.getDiff(commit, parentCommit)
                        .getBytes()));
            } else {
                p.parse(new ByteArrayInputStream(gitDiffService.getDiff(commit)
                        .getBytes()));
            }
            var pe = new Patch();
            if (parentCommit != null) {
                pe.parse(new ByteArrayInputStream(gitDiffService.getDiff(commit, parentCommit, true)
                        .getBytes()));
            } else {
                pe.parse(new ByteArrayInputStream(gitDiffService.getDiff(commit)
                        .getBytes()));
            }
            var exactDiffMap = new HashMap<String, List<GitCommitDiffLineChanges>>();
            pe.getFiles().stream().flatMap(fileHeader ->
                            fileHeader.getHunks().stream().map(hunk -> {
                                var lineChanges = new GitCommitDiffLineChanges();
                                lineChanges.setNewStartLine(hunk.getNewStartLine());
                                lineChanges.setNewLineCount(hunk.getNewLineCount());
                                lineChanges.setOldStartLine(hunk.getOldImage().getStartLine());
                                lineChanges.setOldLineCount(hunk.getOldImage().getLineCount());
                                return lineChanges;
                            }).map(hunkEntity -> Pair.of(fileHeader, hunkEntity))
                    )
                    .forEach(x -> {
                        if (!exactDiffMap.containsKey(x.getFirst().getNewId().name())) {
                            exactDiffMap.put(x.getFirst().getNewId().name(), new ArrayList<>());
                        }
                        exactDiffMap.get(x.getFirst().getNewId().name()).add(x.getSecond());
                    });
            var entities = new ArrayList<GitCommitDiffFile>();
            for (FileHeader fileHeader : p.getFiles()) {
                if (CollectionUtils.isEmpty(fileHeader.getHunks())) {
                    LOG.info("Skipping pure mode change of {} ", fileHeader.getNewPath());
                    continue;
                }
                LOG.info("Collecting hunk information for {} ", fileHeader.getNewPath());
                var entity = new GitCommitDiffFile();
                entity.setCommitRelationship(gitCommitRelationship);
                switch (fileHeader.getChangeType()) {
                    case ADD -> {
                        entity.setNewFilePath(fileHeader.getNewPath());
                        entity.setNewFileObjectId(resolveObjectId(gitObjectReader, fileHeader.getNewId()).getName());
                    }
                    case DELETE -> {
                        entity.setOldFilePath(fileHeader.getOldPath());
                        entity.setOldFileObjectId(resolveObjectId(gitObjectReader, fileHeader.getOldId()).getName());
                    }
                    // TODO: Check if COPY requires special treatment
                    default -> {
                        entity.setNewFilePath(fileHeader.getNewPath());
                        entity.setNewFileObjectId(resolveObjectId(gitObjectReader, fileHeader.getNewId()).getName());
                        entity.setOldFilePath(fileHeader.getOldPath());
                        entity.setOldFileObjectId(resolveObjectId(gitObjectReader, fileHeader.getOldId()).getName());
                    }
                }

                entity.setChangeType(fileHeader.getChangeType());
                entities.add(entity);
                entity.setGitCommitDiffHunks(new ArrayList<>());
                entity.setGitCommitDiffLineChanges(new ArrayList<>());
                BlameResult blame = null;
                for (HunkHeader hunk : fileHeader.getHunks()) {
                    LOG.info("Processing {} {}", hunk.toString(), commit.getId().getName());
                    var hunkEntity = new GitCommitDiffHunk();
                    hunkEntity.setNewStartLine(hunk.getNewStartLine());
                    hunkEntity.setNewLineCount(hunk.getNewLineCount());
                    hunkEntity.setOldStartLine(hunk.getOldImage().getStartLine());
                    hunkEntity.setOldLineCount(hunk.getOldImage().getLineCount());
                    hunkEntity.setDependencyCommitShaSet(new HashSet<>());
                    hunkEntity.setDiffFile(entity);
                    entity.getGitCommitDiffHunks().add(hunkEntity);
                    var lineChanges = exactDiffMap.get(fileHeader.getNewId().name());
                    lineChanges.forEach(x -> x.setDiffFile(entity));
                    entity.getGitCommitDiffLineChanges().addAll(lineChanges);
                    if (fileHeader.getChangeType() != DiffEntry.ChangeType.ADD && parentCommit != null) {
                        if (blame == null) {
                            blame = git.blame()
                                    .setStartCommit(parentCommit.getId())
                                    .setFollowFileRenames(true)
                                    .setFilePath(fileHeader.getOldPath())
                                    .call();
                        }
                        blame.computeRange(hunk.getOldImage().getStartLine(),
                                hunk.getOldImage().getLineCount());
                        var line = hunk.getOldImage().getStartLine() - 1;
                        var relatedAncestorCommits = new HashSet<RevCommit>();
                        for (var i = 0; i < hunk.getOldImage().getLineCount(); ) {
                            var srcCommit = blame.getSourceCommit(line + i++);
                            relatedAncestorCommits.add(srcCommit);
                            hunkEntity.getDependencyCommitShaSet().add(srcCommit.getName());
                        }
                        LOG.info("{} {} depends on {}",
                                hunk,
                                commit.getId().getName(),
                                relatedAncestorCommits.stream().map(RevCommit::getName).collect(
                                        Collectors.joining(",")));
                    }
                }

            }
            return entities;
        } catch (GitAPIException | IOException e) {
            // No information can be gathered for this commit
            LOG.error("Collecting modified/added files between {} and {} failed.",
                    commit.getId().getName(),
                    parentCommit == null ? "root" : parentCommit.getId().getName(),
                    e
            );
            return new ArrayList<>();
        }
    }

    private ObjectId resolveObjectId(
            ObjectReader objectReader,
            AbbreviatedObjectId abbreviatedObjectId
    ) throws IOException {
        var resolved = objectReader.resolve(abbreviatedObjectId);
        if (resolved.isEmpty()) {
            throw new FileNotFoundException();
        }
        return resolved.stream().findFirst().get();
    }

}
