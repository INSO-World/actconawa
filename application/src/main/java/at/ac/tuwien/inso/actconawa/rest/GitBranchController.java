package at.ac.tuwien.inso.actconawa.rest;

import at.ac.tuwien.inso.actconawa.api.BranchService;
import at.ac.tuwien.inso.actconawa.dto.GitBranchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "/branch", produces = APPLICATION_JSON_VALUE)
public class GitBranchController {
    private final BranchService branchService;

    public GitBranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping()
    public Page<GitBranchDto> findAllBranches(Pageable pageable) {
        return branchService.findAll(pageable);
    }

}
