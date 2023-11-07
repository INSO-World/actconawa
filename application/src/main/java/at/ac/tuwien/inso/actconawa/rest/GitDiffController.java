package at.ac.tuwien.inso.actconawa.rest;

import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffHunkDto;
import at.ac.tuwien.inso.actconawa.service.GitDiffService;
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

    private final GitDiffService gitDiffService;

    public GitDiffController(GitDiffService gitDiffService) {
        this.gitDiffService = gitDiffService;
    }

    @GetMapping("/hunks")
    public List<GitCommitDiffHunkDto> findDiffHunks(
            @RequestParam(name = "commit-diff-file-id") UUID commitDiffFileId
    ) {
        return gitDiffService.findGitCommitDiffHunksByDiffFileId(commitDiffFileId);
    }

}
