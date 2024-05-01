package at.ac.tuwien.inso.actconawa.index;

import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffFile;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffHunk;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffLineChange;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitRelationship;
import at.ac.tuwien.inso.actconawa.repository.GitCommitDiffFileRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitDiffHunkRepository;
import at.ac.tuwien.inso.actconawa.repository.GitCommitDiffLineChangeRepository;
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
import java.util.Optional;
import java.util.stream.Collectors;


@Component
@Order(3)
public class GitDiffIndexer implements Indexer {

    private static final Logger LOG = LoggerFactory.getLogger(GitDiffIndexer.class);

    public static final int DIFF_LINE_CONTEXT = 3;

    public static final int DIFF_LINE_CONTEXTLESS = 0;

    private final Git git;

    private final GitCommitService gitCommitService;

    private final GitDiffService gitDiffService;

    private final GitCommitDiffHunkRepository gitDiffHunkRepository;

    private final GitCommitDiffLineChangeRepository gitCommitDiffLineChangeRepository;

    private final GitCommitDiffFileRepository gitCommitDiffFileRepository;

    private final GitCommitRelationshipRepository gitCommitRelationshipRepository;

    public GitDiffIndexer(Git git, GitCommitService gitCommitService, GitDiffService gitDiffService, GitCommitDiffHunkRepository gitDiffHunkRepository, GitCommitDiffLineChangeRepository gitCommitDiffLineChangeRepository, GitCommitDiffFileRepository gitCommitDiffFileRepository, GitCommitRelationshipRepository gitCommitRelationshipRepository) {
        this.git = git;
        this.gitCommitService = gitCommitService;
        this.gitDiffService = gitDiffService;
        this.gitDiffHunkRepository = gitDiffHunkRepository;
        this.gitCommitDiffLineChangeRepository = gitCommitDiffLineChangeRepository;
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

        gitCommitDiffLineChangeRepository.saveAll(
                commitDiffFiles.stream()
                        .map(GitCommitDiffFile::getGitCommitDiffLineChanges)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
        gitDiffHunkRepository.saveAll(
                commitDiffFiles.stream()
                        .map(GitCommitDiffFile::getGitCommitDiffHunks)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .peek(h -> {
                            if (!CollectionUtils.isEmpty(h.getDependencyCommitShaSet())) {
                                h.setDependencies(new ArrayList<>());
                                h.getDependencyCommitShaSet()
                                        .forEach(x -> h.getDependencies().add(commitCache.get(x)));
                            }
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
            var patch = new Patch();
            patch.parse(new ByteArrayInputStream(
                    gitDiffService.getDiff(commit, parentCommit, DIFF_LINE_CONTEXT).getBytes()));
            var exactDiffMap = processLineChanges(commit, parentCommit);
            var entities = new ArrayList<GitCommitDiffFile>();
            for (FileHeader fileHeader : patch.getFiles()) {
                if (CollectionUtils.isEmpty(fileHeader.getHunks())) {
                    LOG.info("Skipping pure mode change of {} ", fileHeader.getNewPath());
                    continue;
                }
                var entity = new GitCommitDiffFile();
                switch (fileHeader.getChangeType()) {
                    case ADD -> {
                        LOG.info("FileHeader hunk information collected for {} (Added)", fileHeader.getNewPath());
                        entity.setNewFilePath(fileHeader.getNewPath());
                        entity.setNewFileObjectId(resolveObjectId(gitObjectReader, fileHeader.getNewId()).getName());
                    }
                    case DELETE -> {
                        LOG.info("FileHeader hunk information collected for {} (Deleted)", fileHeader.getOldPath());
                        entity.setOldFilePath(fileHeader.getOldPath());
                        entity.setOldFileObjectId(resolveObjectId(gitObjectReader, fileHeader.getOldId()).getName());
                    }
                    // TODO: Check if COPY requires special treatment
                    default -> {
                        LOG.info("FileHeader hunk information collected for {} (former {})",
                                fileHeader.getNewPath(), fileHeader.getOldPath());
                        entity.setNewFilePath(fileHeader.getNewPath());
                        entity.setNewFileObjectId(resolveObjectId(gitObjectReader, fileHeader.getNewId()).getName());
                        entity.setOldFilePath(fileHeader.getOldPath());
                        entity.setOldFileObjectId(resolveObjectId(gitObjectReader, fileHeader.getOldId()).getName());
                    }
                }
                entity.setCommitRelationship(gitCommitRelationship);
                entity.setChangeType(fileHeader.getChangeType());
                entity.setGitCommitDiffHunks(new ArrayList<>());
                entity.setGitCommitDiffLineChanges(new ArrayList<>());

                linkLineChanges(fileHeader, exactDiffMap, entity);
                processHunks(commit, parentCommit, fileHeader, entity);

                entities.add(entity);
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

    private HashMap<String, List<GitCommitDiffLineChange>> processLineChanges(RevCommit commit, RevCommit parentCommit)
            throws IOException {
        var exactDiffMap = new HashMap<String, List<GitCommitDiffLineChange>>();
        var contextLessPatch = new Patch();
        contextLessPatch.parse(new ByteArrayInputStream(
                gitDiffService.getDiff(commit, parentCommit, DIFF_LINE_CONTEXTLESS).getBytes()));

        contextLessPatch.getFiles().stream()
                .flatMap(fileHeader -> fileHeader.getHunks().stream().map(hunk -> {
                    var lineChanges = new GitCommitDiffLineChange();
                    lineChanges.setNewStartLine(hunk.getNewStartLine());
                    lineChanges.setNewLineCount(hunk.getNewLineCount());
                    lineChanges.setOldStartLine(hunk.getOldImage().getStartLine());
                    lineChanges.setOldLineCount(hunk.getOldImage().getLineCount());
                    return lineChanges;
                }).map(hunkEntity -> Pair.of(fileHeader, hunkEntity)))
                .forEach(x -> {
                    if (!exactDiffMap.containsKey(x.getFirst().getNewId().name())) {
                        exactDiffMap.put(x.getFirst().getNewId().name(), new ArrayList<>());
                    }
                    exactDiffMap.get(x.getFirst().getNewId().name()).add(x.getSecond());
                });
        return exactDiffMap;
    }

    private static void linkLineChanges(FileHeader fileHeader, HashMap<String, List<GitCommitDiffLineChange>> exactDiffMap, GitCommitDiffFile entity) {
        exactDiffMap.get(fileHeader.getNewId().name()).forEach(gitCommitDiffLineChange -> {
            gitCommitDiffLineChange.setDiffFile(entity);
            entity.getGitCommitDiffLineChanges().add(gitCommitDiffLineChange);
        });
    }

    private void processHunks(RevCommit commit, RevCommit parentCommit, FileHeader fileHeader, GitCommitDiffFile entity)
            throws GitAPIException, IOException {
        Optional<BlameResult> blame = parentCommit == null ? Optional.empty() : Optional.ofNullable(git.blame()
                .setStartCommit(parentCommit.getId())
                .setFollowFileRenames(true)
                .setFilePath(fileHeader.getOldPath())
                .call());
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

            // blame being null means the parent commit is null, which means there are no textual dependencies.
            if (blame.isPresent()) {
                if (fileHeader.getChangeType() == DiffEntry.ChangeType.ADD) {
                    throw new IllegalStateException("Parent commit seems to be null but changetype is not ADD");
                }

                if (hunk.getOldImage().getLineCount() == 0) {
                    // if there is no line count, that means the file must have been emptied (or created empty)
                    // in the previous commit. Therefore the commit depends on the previous commit of that file.
                    var pathHistoryIterator = git.log()
                            .addPath(fileHeader.getOldPath())
                            .add(commit.getId())
                            .setMaxCount(2)
                            .call()
                            .iterator();
                    RevCommit prevCommit = null;
                    while (pathHistoryIterator.hasNext()) {
                        prevCommit = pathHistoryIterator.next();
                    }
                    if (prevCommit != null) {
                        hunkEntity.getDependencyCommitShaSet().add(prevCommit.getName());
                    }
                } else {
                    hunkEntity.getDependencyCommitShaSet().addAll(
                            findContextualDependencies(hunk, blame.get())
                    );
                }
            }
        }
    }

    private HashSet<String> findContextualDependencies(HunkHeader hunk, BlameResult blame)
            throws IOException {

        blame.computeRange(hunk.getOldImage().getStartLine(), hunk.getOldImage().getLineCount());

        var line = hunk.getOldImage().getStartLine() - 1;
        var relatedAncestorCommits = new HashSet<String>();
        for (var i = 0; i < hunk.getOldImage().getLineCount(); ) {
            var srcCommit = blame.getSourceCommit(line + i++);
            relatedAncestorCommits.add(srcCommit.getName());
        }
        LOG.info("Dependency on on commit(s) {} found", String.join(",", relatedAncestorCommits));
        return relatedAncestorCommits;
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
