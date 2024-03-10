package at.ac.tuwien.inso.actconawa.rest;

import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffCodeChangeDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffHunkDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffLineChangeDto;
import at.ac.tuwien.inso.actconawa.dto.GitPatchDto;
import at.ac.tuwien.inso.actconawa.service.GitDiffService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/diffs", produces = APPLICATION_JSON_VALUE)
public class GitDiffController {

    public static final String DEFAULT_DIFF_LINE_CONTEXT = "3";

    private final GitDiffService gitDiffService;

    public GitDiffController(GitDiffService gitDiffService) {
        this.gitDiffService = gitDiffService;
    }

    @GetMapping("/hunks")
    public List<GitCommitDiffHunkDto> findDiffHunks(
            @NotNull
            @RequestParam(name = "commit-diff-file-id") UUID commitDiffFileId
    ) {
        return gitDiffService.findGitCommitDiffHunksByDiffFileId(commitDiffFileId);
    }

    @GetMapping("/line-changes")
    public List<GitCommitDiffLineChangeDto> findDiffLineChanges(
            @NotNull
            @RequestParam(name = "commit-diff-file-id") UUID commitDiffFileId
    ) {
        return gitDiffService.findGitCommitLineChangesByDiffFileId(commitDiffFileId);
    }

    @GetMapping("/code-changes")
    public List<GitCommitDiffCodeChangeDto> findDiffCodeChanges(
            @NotNull
            @RequestParam(name = "commit-diff-file-id") UUID commitDiffFileId
    ) {
        return gitDiffService.findGitCommitCodeChangesByDiffFileId(commitDiffFileId);
    }

    @GetMapping(value = "/patch")
    public GitPatchDto getPatch(
            @NotNull
            @RequestParam(name = "commit-id") UUID commitId,
            @RequestParam(name = "parent-commit-id") UUID parentCommitId,
            @PositiveOrZero
            @RequestParam(name = "context-lines", defaultValue = DEFAULT_DIFF_LINE_CONTEXT) int contextLines
    ) {
        return gitDiffService.getDiff(commitId, parentCommitId, contextLines);
    }

}
