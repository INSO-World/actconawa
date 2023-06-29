package at.ac.tuwien.inso.actconawa.mapper;

import at.ac.tuwien.inso.actconawa.dto.GitBranchDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitRelationshipDto;
import at.ac.tuwien.inso.actconawa.persistence.GitBranch;
import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitRelationship;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface GitMapper {

    default UUID idFromModel(GitBranch gitBranch) {
        return gitBranch.getId();
    }

    default UUID idFromModel(GitCommit branch) {
        return branch.getId();
    }

    @Mapping(source = "headCommit", target = "headCommitId")
    @Mapping(source = "remoteHead", target = "remoteHead")
    @Mapping(source = "containingExclusiveCommits", target = "containingExclusiveCommits")
    GitBranchDto mapModelToDto(GitBranch branch);

    @Mapping(source = "parent", target = "parentId")
    @Mapping(source = "child", target = "childId")
    GitCommitRelationshipDto mapModelToDto(GitCommitRelationship relationship);


    @Mapping(source = "branches", target = "branchIds")
    @Mapping(target = "parentIds", expression = "java(getParentCommitIds(commit.getParents()))")
    GitCommitDto mapModelToDto(GitCommit commit);

    // TODO: This one is fishy
    default List<UUID> getParentCommitIds(List<GitCommitRelationship> gitCommitRelationships) {
        return gitCommitRelationships.stream()
                .map(x -> x.getId().getParent()).collect(Collectors.toList());
    }


}
