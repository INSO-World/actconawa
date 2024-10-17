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
  GitDiffControllerService
} from "../../api";
import { EMPTY, expand, lastValueFrom, tap } from "rxjs";
import { CompositeKeyMap } from "../utils/CompositeKeyMap";
import { NodeDefinition } from "cytoscape";
import { v4 as uuidv4 } from "uuid";
import { ExtendedGitCommitDto } from "../utils/ExtendedGitCommitDto";

@Injectable({
  providedIn: 'root'
})
export class GitService {

  private readonly BRANCH_PAGE_SIZE = 1000;

  private readonly COMMIT_PAGE_SIZE = 100;

  private readonly BRANCH_TRACKING_PAGE_SIZE = 1000;

  private branchById = new Map<string, GitBranchDto>;

  private commitById = new Map<string, ExtendedGitCommitDto>;

  private commitCompound = new Map<string, NodeDefinition>;

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

  async getCommits(): Promise<ExtendedGitCommitDto[]> {
    if (this.commitById.size === 0) {
      await this.loadCommits();
    }
    return Array.from(this.commitById.values());
  }

  async getCommitCompounds(): Promise<NodeDefinition[]> {
    if (this.commitById.size === 0) {
      await this.loadCommits();
    }
    return Array.from(this.commitCompound.values());
  }

  async getCommitById(commitId: string): Promise<GitCommitDto | undefined> {
    // TODO: remove this extra loading of commits?
    return this.commitById.get(commitId) || (await this.getCommitAndAncestory(commitId, 0))[ 0 ];
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

  async getCommitAndAncestory(commitId: string, maxDepth: number = 100) {
    // TODO: Document that this is not cached.
    return await lastValueFrom(this.gitCommitService.findAncestors(commitId, maxDepth));
  }

  private async loadCommits(): Promise<void> {
    if (this.commitById.size > 0) {
      return;
    }

    const visited = new Set<string>;
    const stack: string[] = [];

    // Load branch heads
    for (const branchDto of await this.getBranches()) {
      const headCommitId = branchDto.headCommitId;
      const headCommit = await this.getCommitById(headCommitId || "");
      // Only load the branch head commits that have no children (== leafs). The others will be loaded anyway
      if (headCommitId && headCommit?.childIds?.length == 0) {
        stack.push(headCommitId);
      }
    }

    let currentCommitId: string | undefined;
    let compositeNode: NodeDefinition = {data: {id: uuidv4()}};
    while ((currentCommitId = stack.pop()) !== undefined) {
      if (visited.has(currentCommitId)) {
        continue;
      }
      const loadedCommits = await lastValueFrom(this.gitCommitService.findAncestors(currentCommitId,
              this.COMMIT_PAGE_SIZE));
      const extendedLoadedCommits = loadedCommits
              .filter(commit => !this.commitById.has(commit.id || ""))
              .map(commit => {
                const extendedCommit = commit as ExtendedGitCommitDto;
                this.commitById.set(commit.id || "", extendedCommit);
                return extendedCommit;
              });
      let index = 0;
      for (const extendedLoadedCommit of extendedLoadedCommits) {
        index++;
        if (visited.has(extendedLoadedCommit.id || "")) {
          break;
        }
        if (extendedLoadedCommit.parentIds &&
                (extendedLoadedCommit.parentIds.length > 1 || index === extendedLoadedCommits.length)) {
          extendedLoadedCommit.parentIds.forEach(parentId => stack.push(parentId));
        }
        visited.add(extendedLoadedCommit.id || "");

        // Composite node handling
        if ((extendedLoadedCommit.parentIds && extendedLoadedCommit.parentIds?.length != 1)
                || (extendedLoadedCommit.headOfBranchesIds && extendedLoadedCommit.headOfBranchesIds?.length > 0)
        ) {
          // new composite node required
          compositeNode = {data: {id: uuidv4()}};
          // those commits may not be part of a composite
          extendedLoadedCommit.parent = undefined;

        } else {
          if (extendedLoadedCommit.childIds && extendedLoadedCommit.childIds?.length > 1) {
            // new composite node required
            compositeNode = {data: {id: uuidv4()}};
          }
          if (compositeNode.data.id && !this.commitCompound.has(compositeNode.data.id)) {
            this.commitCompound.set(compositeNode.data.id, compositeNode);
          }
          extendedLoadedCommit.parent = compositeNode.data.id || "";
        }
      }
    }
  }

}
