package at.ac.tuwien.inso.actconawa.rest;

import at.ac.tuwien.inso.actconawa.api.CommitService;
import at.ac.tuwien.inso.actconawa.dto.GitCommitBranchRelationshipDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffFileDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitGroupDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitRelationshipDto;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/commits", produces = APPLICATION_JSON_VALUE)
public class GitCommitController {

    private final String DEFAULT_COMMIT_ANCESTOR_MAX_DEPTH = "10";
    private final CommitService commitService;

    public GitCommitController(CommitService commitService) {
        this.commitService = commitService;
    }

    @GetMapping
    public Page<GitCommitDto> findAllCommits(Pageable pageable) {
        return commitService.findAll(pageable);
    }

    @GetMapping("/relations")
    public Page<GitCommitRelationshipDto> findAllCommitRelations(Pageable pageable) {
        return commitService.findAllRelations(pageable);
    }

    @GetMapping("/groups")
    public Page<GitCommitGroupDto> findAllCommitGroups(Pageable pageable) {
        return commitService.findAllCommitGroups(pageable);
    }

    @GetMapping("/modified-files")
    public List<GitCommitDiffFileDto> findAllModifiedFiles(
            @NotNull
            @RequestParam UUID commitId,
            @NotNull
            @RequestParam UUID parentCommitId
    ) {
        return commitService.findModifiedFiles(commitId, parentCommitId);
    }

    @GetMapping("/{commitId}/ancestors")
    public List<GitCommitDto> findAncestors(
            @NotNull
            @PathVariable UUID commitId,
            @RequestParam(defaultValue = DEFAULT_COMMIT_ANCESTOR_MAX_DEPTH) int maxDepth) {
        return commitService.findAncestors(commitId, maxDepth);
    }

    @GetMapping("/{commitId}/branches")
    public GitCommitBranchRelationshipDto findBranches(
            @NotNull
            @PathVariable UUID commitId) {
        return commitService.findBranches(commitId);
    }

}
