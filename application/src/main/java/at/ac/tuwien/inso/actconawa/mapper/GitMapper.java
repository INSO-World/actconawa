package at.ac.tuwien.inso.actconawa.mapper;

import at.ac.tuwien.inso.actconawa.dto.GitBranchDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitRelationshipDto;
import at.ac.tuwien.inso.actconawa.persistence.GitBranch;
import at.ac.tuwien.inso.actconawa.persistence.GitBranchCommit;
import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitRelationship;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface GitMapper {

    default Long idFromModel(GitBranch gitBranch) {
        return gitBranch.getId();
    }

    default Long getBranchIdOfBranchCommit(GitBranchCommit gitBranchCommit) {
        return gitBranchCommit.getId().getBranch();
    }

    default List<Long> getBranchIdsOfBranchCommits(Collection<GitBranchCommit> gitBranchCommits) {
        return gitBranchCommits.stream()
                .map(this::getBranchIdOfBranchCommit)
                .collect(Collectors.toList());
    }

    default Long idFromModel(GitCommit branch) {
        return branch.getId();
    }

    @Mapping(source = "headCommit", target = "headCommitId")
    GitBranchDto mapModelToDto(GitBranch branch);

    @Mapping(source = "parent", target = "parentId")
    @Mapping(source = "child", target = "childId")
    GitCommitRelationshipDto mapModelToDto(GitCommitRelationship relationship);


    @Mapping(target = "branchIds", expression = "java(getBranchIdsOfBranchCommits(commit.getBranchRelationships()))")
    GitCommitDto mapModelToDto(GitCommit commit);


}
