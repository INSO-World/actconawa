import { Injectable } from '@angular/core';
import {
  GitBranchControllerService,
  GitBranchDto,
  GitCommitControllerService,
  GitCommitDto,
  GitDiffControllerService
} from "../../api";
import { EMPTY, expand, filter, lastValueFrom, mergeMap, Observable, of, tap } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class GitService {

  private BRANCH_PAGE_SIZE = 10;

  private COMMIT_QUERY_DEPTH = 3;

  private branchById = new Map<string, GitBranchDto>;

  private commitById = new Map<string, GitCommitDto>;

  private branchIdsByCommitId = new Map<string, string[]>;

  constructor(
          private gitCommitService: GitCommitControllerService,
          private gitBranchService: GitBranchControllerService,
          private gitDiffControllerService: GitDiffControllerService
  ) {
  }

  async getCommits(): Promise<GitCommitDto[]> {
    if (this.commitById.size === 0) {
      await this.loadCommitsForBranches();
    }
    return Array.from(this.commitById.values());
  }

  async getCommitById(commitId: string): Promise<GitCommitDto | undefined> {
    if (this.commitById.size === 0) {
      await this.getCommits();
    }
    return this.branchById.get(commitId);
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

  loadChangesOfCommit(commit: GitCommitDto) {
    if (commit.id && commit.parentIds?.length == 1) {
      lastValueFrom(this.gitCommitService.findAllModifiedFiles(commit.id, commit.parentIds[ 0 ]).pipe(
              tap(x => console.log(x)),
              mergeMap(x => x),
              tap(x =>
                      this.gitDiffControllerService.findDiffCodeChanges(x.id || "")
                              .subscribe({
                                next(v) {
                                  console.log(v);
                                }
                              })
              ),
              tap(x =>
                      this.gitDiffControllerService.findDiffHunks(x.id || "")
                              .subscribe({
                                next(v) {
                                  console.log(v);
                                }
                              })
              ),
              tap(x =>
                      this.gitDiffControllerService.findDiffLineChanges(x.id || "")
                              .subscribe({
                                next(v) {
                                  console.log(v);
                                }
                              })
              ),
      ), {defaultValue: EMPTY})
    }
  }

  private async loadBranches() {
    const branchPages = this.gitBranchService.findAllBranches({page: 0, size: this.BRANCH_PAGE_SIZE}).pipe(
            expand(branchesPage => {
              if (!branchesPage.last && branchesPage.number != undefined) {
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

  private async loadCommitsForBranches() {
    if (this.branchById.size === 0) {
      await this.getBranches();
    }
    for (const branch of this.branchById.values()) {
      if (branch.headCommitId) {
        await this.loadCommitsAndAncestors(branch.headCommitId, true);
      }
    }
  }

  private async loadCommitsAndAncestors(commitId: string, loadAll: boolean): Promise<Observable<never>> {
    if (this.commitById.has(commitId)) {
      return EMPTY;
    }
    const commits = await lastValueFrom(this.gitCommitService.findAncestors(commitId, this.COMMIT_QUERY_DEPTH),
            {defaultValue: undefined});
    const parentIds = commits?.flatMap(commit => {
      this.commitById.set(commit.id || "", commit);
      return commit.parentIds || [];
    }) || [];
    if (loadAll) {
      return lastValueFrom(of(parentIds).pipe(
              mergeMap(parentIds => parentIds),
              filter(parentId => !this.commitById.has(parentId)),
              mergeMap(parentId => this.loadCommitsAndAncestors(parentId, loadAll))
      ), {defaultValue: EMPTY})

    } else {
      return EMPTY;
    }
  }

}
