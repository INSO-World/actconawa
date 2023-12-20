import { Component, ElementRef, OnDestroy, OnInit } from '@angular/core';
import { GitBranchControllerService, GitBranchDto, GitCommitControllerService, GitCommitDto } from '../../../api';
import { EMPTY, expand, filter, lastValueFrom, mergeMap, Observable, of, tap } from "rxjs";
import * as cytoscape from 'cytoscape';
import { EdgeDefinition, NodeDefinition } from 'cytoscape';
import * as dagre from 'cytoscape-dagre';
import { DagreLayoutOptions } from 'cytoscape-dagre';

@Component({
  selector: 'app-active-conflict-awareness',
  templateUrl: './active-conflict-awareness.component.html',
  styleUrls: ['./active-conflict-awareness.component.scss']
})
export class ActiveConflictAwarenessComponent implements OnInit, OnDestroy {
  private BRANCH_PAGE_SIZE = 10;

  private COMMIT_QUERY_DEPTH = 3;

  protected loading = true;

  protected cy: cytoscape.Core | undefined;

  protected selectedCommit?: GitCommitDto;

  protected selectedCommitsBranches?: GitBranchDto[];

  protected branchById = new Map<string, GitBranchDto>;

  protected commitById = new Map<string, GitCommitDto>;

  protected branchIdsByCommitId = new Map<string, string[]>;

  protected cytoscapeCommits: NodeDefinition[] = []

  protected cytoscapeCommitRelationships: EdgeDefinition[] = []

  constructor(private gitCommitService: GitCommitControllerService, private gitBranchService: GitBranchControllerService, private el: ElementRef) {
  }

  async ngOnInit() {

    await this.loadBranches()
    await this.loadCommitsForBranches();
    console.log(this.branchById)
    console.log(this.commitById)
    for (const commit of this.commitById.values()) {
      this.cytoscapeCommits.push({data: commit, selectable: true, selected: false})
      commit.parentIds?.forEach(parentId => this.cytoscapeCommitRelationships.push({
        data: {
          id: commit.id
                  + "-"
                  + parentId, source: parentId, target: commit.id || ""
        }
      }))
    }
    cytoscape.use(dagre);
    this.cy = cytoscape({
      container: this.el.nativeElement.querySelector('#cy'),
      elements: {
        nodes: this.cytoscapeCommits,
        edges: this.cytoscapeCommitRelationships
      },
      layout: {
        name: 'dagre',
        rankDir: "LR"
      } as DagreLayoutOptions
    });
    this.loading = false;
    this.cy.on('click', 'node', (e: any) => {
      this.selectedCommitsBranches = undefined
      const commit = e.target._private.data;
      this.loadBranchesForCommit(commit).then(() => {
        this.selectedCommit = commit;
        this.selectedCommitsBranches = (this.branchIdsByCommitId.get(commit.id || "") || [])
                .map(b => this.branchById.get(b))
                .filter(b => b != undefined)
                .map(b => b as GitBranchDto)
      });
    })
  }

  ngOnDestroy(): void {
    this.commitById.clear();
    this.branchById.clear();
  }

  async loadBranches() {
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

  async loadCommitsForBranches() {
    for (const branch of this.branchById.values()) {
      if (branch.headCommitId) {
        await this.loadCommitsAndAncestors(branch.headCommitId, true);
      }
    }
  }

  async loadBranchesForCommit(commit: GitCommitDto) {
    if (!this.branchIdsByCommitId.has(commit.id || "")) {
      await lastValueFrom(this.gitCommitService.findBranches(commit.id || "")
              .pipe(tap(b =>
                      this.branchIdsByCommitId.set(b.commitId || "", b.branchIds || []))), {defaultValue: EMPTY}
      );
    }
  }

  async loadCommitsAndAncestors(commitId: string, loadAll: boolean): Promise<Observable<never>> {
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
