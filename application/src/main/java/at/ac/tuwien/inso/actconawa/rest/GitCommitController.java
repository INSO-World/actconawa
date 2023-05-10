package at.ac.tuwien.inso.actconawa.rest;

import at.ac.tuwien.inso.actconawa.api.CommitService;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/commit", produces = APPLICATION_JSON_VALUE)
public class GitCommitController {

    private final CommitService commitService;

    public GitCommitController(CommitService commitService) {
        this.commitService = commitService;
    }

    @GetMapping
    public Page<GitCommitDto> findAllCommits(Pageable pageable) {
        return commitService.findAll(pageable);
    }

}
