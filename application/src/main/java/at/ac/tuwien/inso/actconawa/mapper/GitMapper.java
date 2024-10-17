package at.ac.tuwien.inso.actconawa.mapper;

import at.ac.tuwien.inso.actconawa.dto.GitBranchDto;
import at.ac.tuwien.inso.actconawa.dto.GitBranchTrackingStatusDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitBranchRelationshipDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffCodeChangeDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffFileDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffHunkDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDiffLineChangeDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitGroupDto;
import at.ac.tuwien.inso.actconawa.dto.GitCommitRelationshipDto;
import at.ac.tuwien.inso.actconawa.persistence.CodeChange;
import at.ac.tuwien.inso.actconawa.persistence.GitBranch;
import at.ac.tuwien.inso.actconawa.persistence.GitBranchTrackingStatus;
import at.ac.tuwien.inso.actconawa.persistence.GitCommit;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffFile;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffHunk;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitDiffLineChange;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitGroup;
import at.ac.tuwien.inso.actconawa.persistence.GitCommitRelationship;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface GitMapper {

    default UUID idFromModel(GitBranch gitBranch) {
        return gitBranch.getId();
    }

    default UUID idFromModel(GitCommit branch) {
        if (branch == null) {
            return null;
        }
        return branch.getId();
    }

    default UUID idFromModel(GitCommitGroup group) {
        if (group == null) {
            return null;
        }
        return group.getId();
    }

    default UUID idFromModel(GitCommitDiffFile diffFile) {
        return diffFile.getId();
    }

    @Mapping(source = "headCommit", target = "headCommitId")
    @Mapping(source = "remoteHead", target = "remoteHead")
    @Mapping(source = "containingExclusiveCommits", target = "containingExclusiveCommits")
    GitBranchDto mapModelToDto(GitBranch branch);

    @Mapping(source = "branchA", target = "branchAId")
    @Mapping(source = "branchB", target = "branchBId")
    @Mapping(source = "mergeBase", target = "mergeBaseCommitId")
    GitBranchTrackingStatusDto mapModelToDto(GitBranchTrackingStatus branchTrackingStatus);

    @Mapping(source = "parent", target = "parentId")
    @Mapping(source = "child", target = "childId")
    GitCommitRelationshipDto mapModelToDto(GitCommitRelationship relationship);

    GitCommitGroupDto mapModelToDto(GitCommitGroup group);

    @Mapping(source = "group", target = "groupId")
    @Mapping(target = "headOfBranchesIds", expression = "java(getHeadOfBranchesIds(commit.getHeadOfBranches()))")
    @Mapping(target = "parentIds", expression = "java(getParentCommitIds(commit.getParents()))")
    @Mapping(target = "childIds", expression = "java(getChildCommitIds(commit.getChildren()))")
    GitCommitDto mapModelToDto(GitCommit commit);

    @Mapping(source = "branches", target = "branchIds")
    @Mapping(source = "id", target = "commitId")
    GitCommitBranchRelationshipDto mapModelToBranchRelationshipDto(GitCommit commit);

    GitCommitDiffFileDto mapModelToDto(GitCommitDiffFile gitCommitDiffFile);

    @Mapping(source = "diffFile", target = "diffFileId")
    @Mapping(source = "dependencies", target = "commitDependencyIds")
    GitCommitDiffHunkDto mapModelToDto(GitCommitDiffHunk gitCommitDiffFile);

    @Mapping(source = "diffFile", target = "diffFileId")
    GitCommitDiffLineChangeDto mapModelToDto(GitCommitDiffLineChange gitCommitDiffLineChange);

    @Mapping(source = "diffFile", target = "diffFileId")
    GitCommitDiffCodeChangeDto mapModelToDto(CodeChange codeChange);

    default List<UUID> getParentCommitIds(List<GitCommitRelationship> gitCommitRelationships) {
        return gitCommitRelationships.stream()
                .map(GitCommitRelationship::getParent)
                .filter(Objects::nonNull)
                .map(GitCommit::getId)
                .collect(Collectors.toList());
    }

    default List<UUID> getChildCommitIds(List<GitCommitRelationship> gitCommitRelationships) {
        return gitCommitRelationships.stream()
                .map(GitCommitRelationship::getChild)
                .filter(Objects::nonNull)
                .map(GitCommit::getId)
                .collect(Collectors.toList());
    }

    default List<UUID> getHeadOfBranchesIds(List<GitBranch> gitBranches) {
        return gitBranches.stream()
                .filter(Objects::nonNull)
                .map(GitBranch::getId)
                .collect(Collectors.toList());
    }

}
