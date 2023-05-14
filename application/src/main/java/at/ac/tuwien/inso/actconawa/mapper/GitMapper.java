package at.ac.tuwien.inso.actconawa.mapper;

import at.ac.tuwien.inso.actconawa.dto.GitBranchDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDto;
import at.ac.tuwien.inso.actconawa.persistence.GitBranch;
import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface GitMapper {

    default Long idFromModel(GitBranch gitBranch) {
        return gitBranch.getId();
    }

    default Long idFromModel(GitCommit branch) {
        return branch.getId();
    }

    @Mapping(source = "headCommit", target = "headCommitId")
    GitBranchDto mapModelToDto(GitBranch branch);

    @Mapping(source = "parents", target = "parentIds")
    GitCommitDto mapModelToDto(GitCommit commit);

    default Page<GitBranchDto> mapBranchPage(Page<GitBranch> branches) {
        return branches.map(this::mapModelToDto);
    }

    default Page<GitCommitDto> mapCommitPage(Page<GitCommit> commits) {
        return commits.map(this::mapModelToDto);
    }
}
