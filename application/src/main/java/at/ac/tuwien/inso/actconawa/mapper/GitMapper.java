package at.ac.tuwien.inso.actconawa.mapper;

import at.ac.tuwien.inso.actconawa.dto.GitBranchDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitRelationshipDto;
import at.ac.tuwien.inso.actconawa.persistence.GitBranch;
import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitRelationship;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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

    @Mapping(source = "parent", target = "parentId")
    @Mapping(source = "child", target = "childId")
    GitCommitRelationshipDto mapModelToDto(GitCommitRelationship relationship);


    @Mapping(source = "branches", target = "branchIds")
    GitCommitDto mapModelToDto(GitCommit commit);


}
