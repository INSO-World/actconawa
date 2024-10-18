import { Injectable } from '@angular/core';
import {
  GitBranchControllerService,
  GitBranchDto,
  GitBranchTrackingStatusDto,
  GitCommitControllerService,
  GitCommitDiffCodeChangeDto,
  GitCommitDiffFileDto,
  GitCommitDiffHunkDto,
  GitCommitDiffLineChangeDto,
  GitCommitDto,
  GitCommitGroupDto,
  GitDiffControllerService
} from "../../api";
import { EMPTY, expand, lastValueFrom, tap } from "rxjs";
import { CompositeKeyMap } from "../utils/CompositeKeyMap";

@Injectable({
  providedIn: 'root'
})
export class GitService {

  private readonly BRANCH_PAGE_SIZE = 1000;

  private readonly COMMIT_PAGE_SIZE = 100;

  private readonly COMMIT_GROUP_PAGE_SIZE = 1000;

  private readonly BRANCH_TRACKING_PAGE_SIZE = 1000;

  private branchById = new Map<string, GitBranchDto>;

  private commitById = new Map<string, GitCommitDto>;

  private commitGroupsById = new Map<string, GitCommitGroupDto>;

  private branchIdsByCommitId = new Map<string, string[]>;

  private commitDiffFilesByCommitIds = new CompositeKeyMap<string, GitCommitDiffFileDto[]>;

  private trackingStatusByBranchIds = new CompositeKeyMap<string, GitBranchTrackingStatusDto>;

  private changedCodeByCommitDiffFileId =
          new Map<string, GitCommitDiffCodeChangeDto[]>;

  private changedLinesByCommitDiffFileId =
          new Map<string, GitCommitDiffLineChangeDto[]>;

  private hunksByCommitDiffFileId =
          new Map<string, GitCommitDiffHunkDto[]>;

  constructor(
          private gitCommitService: GitCommitControllerService,
          private gitBranchService: GitBranchControllerService,
          private gitDiffControllerService: GitDiffControllerService
  ) {
  }

  async getCommits(): Promise<GitCommitDto[]> {
    if (this.commitById.size === 0) {
      await this.loadCommits();
    }
    return Array.from(this.commitById.values());
  }

  async getCommitGroups(): Promise<GitCommitGroupDto[]> {
    if (this.commitGroupsById.size === 0) {
      await this.loadCommitGroups();
    }
    return Array.from(this.commitGroupsById.values());
  }

  async loadCommitGroups(): Promise<void> {
    const groupPages = this.gitCommitService.findAllCommitGroups({page: 0, size: this.COMMIT_GROUP_PAGE_SIZE}).pipe(
            expand(groupPage => {
              if (!groupPage.last && groupPage.number !== undefined) {
                return this.gitCommitService.findAllCommitGroups({
                  page: groupPage.number + 1,
                  size: this.COMMIT_GROUP_PAGE_SIZE
                });
              } else {
                return EMPTY;
              }
            }),
            tap(groupPage => {
              (groupPage.content || []).forEach(group => {
                if (group.id) {
                  this.commitGroupsById.set(group.id, group);
                }
              });
            }));
    await lastValueFrom(groupPages);
  }

  async getCommitById(commitId: string): Promise<GitCommitDto | undefined> {
    return this.commitById.get(commitId) || (await lastValueFrom(this.gitCommitService.findAncestors(commitId,
            0)))[ 0 ];
  }

  async getBranches(): Promise<GitBranchDto[]> {
    if (this.branchById.size === 0) {
      await this.loadBranches();
    }
    return Array.from(this.branchById.values());
  }

  async getBranchById(branchId: string): Promise<GitBranchDto | undefined> {
    if (this.branchById.size === 0) {
      await this.getBranches();
    }
    return this.branchById.get(branchId);
  }

  async getBranchTrackingStatusByIds(branchAId: string, branchBId: string): Promise<GitBranchTrackingStatusDto | undefined> {
    if (this.trackingStatusByBranchIds.size() === 0) {
      await this.loadBranchTrackingStatus();
    }
    return this.trackingStatusByBranchIds.get(branchAId, branchBId);
  }

  async getBranchTrackingStatusById(branchId: string): Promise<GitBranchTrackingStatusDto[]> {
    if (this.trackingStatusByBranchIds.size() === 0) {
      await this.loadBranchTrackingStatus();
    }
    return this.trackingStatusByBranchIds.getAllOfKey(branchId);
  }

  async getBranchesByCommitId(commitId: string): Promise<GitBranchDto[]> {
    if (!this.branchIdsByCommitId.has(commitId)) {
      await lastValueFrom(this.gitCommitService.findBranches(commitId || "")
              .pipe(tap(b =>
                      this.branchIdsByCommitId.set(b.commitId || "", b.branchIds || []))), {defaultValue: EMPTY}
      );
    }
    return (await Promise.all((this.branchIdsByCommitId.get(commitId) || []).map(b => this.getBranchById(b))))
            .flatMap(b => b ? [b] : []);
  }

  async getModifiedFilesByCommitIds(commitAId: string, commitBId: string) {
    if (!this.commitDiffFilesByCommitIds.has(commitAId, commitBId)) {
      const diffFiles =
              await lastValueFrom(this.gitCommitService.findAllModifiedFiles(commitAId, commitBId))
      this.commitDiffFilesByCommitIds.set(commitAId, commitBId, diffFiles);
      return diffFiles;
    } else {
      return this.commitDiffFilesByCommitIds.get(commitAId, commitBId);
    }
  }

  async getHunksByDiffFileId(diffFileId: string) {
    if (!this.hunksByCommitDiffFileId.has(diffFileId)) {
      const hunks =
              await lastValueFrom(this.gitDiffControllerService.findDiffHunks(diffFileId));
      this.hunksByCommitDiffFileId.set(diffFileId, hunks);
      return hunks;
    } else {
      return this.hunksByCommitDiffFileId.get(diffFileId);
    }
  }

  async getCodeChangesByDiffFileId(diffFileId: string) {
    if (!this.changedCodeByCommitDiffFileId.has(diffFileId)) {
      const codeChanges =
              await lastValueFrom(this.gitDiffControllerService.findDiffCodeChanges(diffFileId));
      this.changedCodeByCommitDiffFileId.set(diffFileId, codeChanges);
      return codeChanges;
    } else {
      return this.changedCodeByCommitDiffFileId.get(diffFileId);
    }
  }

  async getLineChangesByDiffFileId(diffFileId: string) {
    if (!this.changedLinesByCommitDiffFileId.has(diffFileId)) {
      const lines =
              await lastValueFrom(this.gitDiffControllerService.findDiffLineChanges(diffFileId));
      this.changedLinesByCommitDiffFileId.set(diffFileId, lines);
      return lines;
    } else {
      return this.changedLinesByCommitDiffFileId.get(diffFileId);
    }
  }

  async getPatch(commitId: string, parentCommitId: string, contextLines: number) {
    return await lastValueFrom(this.gitDiffControllerService.getPatch(commitId, parentCommitId, contextLines));
  }

  private async loadBranches() {
    const branchPages = this.gitBranchService.findAllBranches({page: 0, size: this.BRANCH_PAGE_SIZE}).pipe(
            expand(branchesPage => {
              if (!branchesPage.last && branchesPage.number !== undefined) {
                return this.gitBranchService.findAllBranches({
                  page: branchesPage.number + 1,
                  size: this.BRANCH_PAGE_SIZE
                });
              } else {
                return EMPTY;
              }
            }),
            tap(branchesPage => {
              (branchesPage.content || []).forEach(branch => {
                if (branch.id) {
                  this.branchById.set(branch.id, branch);
                }
              });
            }));
    await lastValueFrom(branchPages);
  }

  private async loadBranchTrackingStatus(): Promise<void> {
    const branchTrackingPages = this.gitBranchService
            .getAllTrackingStatus({page: 0, size: this.BRANCH_TRACKING_PAGE_SIZE}).pipe(
                    expand(trackingPage => {
                      if (!trackingPage.last && trackingPage.number !== undefined) {
                        return this.gitBranchService.getAllTrackingStatus({
                          page: trackingPage.number + 1,
                          size: this.BRANCH_TRACKING_PAGE_SIZE
                        });
                      } else {
                        return EMPTY;
                      }
                    }),
                    tap(trackingPage => {
                      (trackingPage.content || []).forEach(tracking => {
                        if (tracking.id) {
                          this.trackingStatusByBranchIds.set(tracking.branchAId || "",
                                  tracking.branchBId || "",
                                  tracking);
                        }
                      });
                    }));
    await lastValueFrom(branchTrackingPages);
  }

  private async loadCommits(): Promise<void> {
    if (this.commitById.size > 0) {
      return;
    }
    const commitPages = this.gitCommitService
            .findAllCommits({page: 0, size: this.COMMIT_PAGE_SIZE}).pipe(
                    expand(commitPage => {
                      if (!commitPage.last && commitPage.number !== undefined) {
                        return this.gitCommitService.findAllCommits({
                          page: commitPage.number + 1,
                          size: this.COMMIT_PAGE_SIZE
                        });
                      } else {
                        return EMPTY;
                      }
                    }),
                    tap(commit => {
                      (commit.content || []).forEach(c => {
                        if (c.id) {
                          this.commitById.set(c.id || "", c);
                        }
                      });
                    }));
    await lastValueFrom(commitPages);
  }

}
