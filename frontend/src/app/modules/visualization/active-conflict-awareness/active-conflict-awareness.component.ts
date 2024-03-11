import { Component, ElementRef, inject, OnInit } from '@angular/core';
import { GitBranchDto, GitCommitDto } from '../../../../api';
import cytoscape, { EdgeDefinition, EventObject, NodeDefinition } from 'cytoscape';
import cytoscapeDagre, { DagreLayoutOptions } from 'cytoscape-dagre';
import { GitService } from "../../../services/git.service";
import { ActivatedRoute, Router } from "@angular/router";

@Component({
  selector: 'app-active-conflict-awareness',
  standalone: false,
  templateUrl: './active-conflict-awareness.component.html',
  styleUrls: ['./active-conflict-awareness.component.scss'],
})
export class ActiveConflictAwarenessComponent implements OnInit {

  protected gitService = inject(GitService)

  protected loading = true;

  protected cy: cytoscape.Core | undefined;

  protected selectedCommit?: GitCommitDto;

  protected parentCommits = new Map<string, GitCommitDto>();

  protected selectedCommitsBranches?: GitBranchDto[];

  protected cytoscapeCommits: NodeDefinition[] = []

  protected cytoscapeCommitRelationships: EdgeDefinition[] = []

  constructor(private el: ElementRef, private route: ActivatedRoute, private router: Router) {
  }

  async ngOnInit() {
    for (const commit of await this.gitService.getCommits()) {
      this.cytoscapeCommits.push({data: commit, selectable: true, selected: false})
      commit.parentIds?.forEach(parentId => this.cytoscapeCommitRelationships.push({
        data: {
          id: commit.id
                  + "-"
                  + parentId, source: parentId, target: commit.id || ""
        }
      }))
    }
    const presetCommitId = this.route.snapshot.queryParamMap.get('commitId');
    if (presetCommitId) {
      this.gitService.getCommitById(presetCommitId).then(presetCommit => {
        if (presetCommit) {
          this.selectCommit(presetCommit, true);
        }
      });
    }
    cytoscape.use(cytoscapeDagre);
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
    this.cy.on('click', 'node', (e: EventObject) => {
      this.selectedCommitsBranches = undefined
      const commit = e.target._private.data as GitCommitDto;
      this.selectCommit(commit, false);
    })
  }

  resizeChart() {
    this.cy?.resize();
  }

  selectCommit(commit: GitCommitDto, selectOnGraph: boolean) {
    if (!commit.id) {
      throw new Error('Commit id must not be null')
    }
    this.router.navigate(
            [],
            {
              relativeTo: this.route,
              queryParams: {commitId: commit.id},
              queryParamsHandling: 'merge'
            }
    );
    this.selectedCommit = commit;
    if (selectOnGraph) {
      this.cy?.$('#' + commit.id).select();
    }
    this.parentCommits = new Map<string, GitCommitDto>();
    commit.parentIds
            ?.map(parentId => this.gitService.getCommitById(parentId))
            .forEach(parentPromise => parentPromise.then(parent => {
              if (parent) {
                this.parentCommits.set(parent.id || "", parent)
              }
            }))
    this.gitService.getBranchesByCommitId(commit.id).then(branches => {
      return this.selectedCommitsBranches = branches;
    })
  }
}
