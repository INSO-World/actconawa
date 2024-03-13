import { Component, ElementRef, inject, OnInit } from '@angular/core';
import { GitBranchDto, GitBranchTrackingStatusDto, GitCommitDto } from '../../../../api';
import cytoscape, { EdgeDefinition, EventObject, NodeDefinition } from 'cytoscape';
import cytoscapeDagre, { DagreLayoutOptions } from 'cytoscape-dagre';
import { GitService } from "../../../services/git.service";
import { ActivatedRoute, Router } from "@angular/router";
import { SettingService } from "../../../services/setting.service";
import { MatDialog } from "@angular/material/dialog";
import {
  ActiveConflictAwarenessDiffComponent
} from "./active-conflict-awareness-diff/active-conflict-awareness-diff.component";

@Component({
  selector: 'app-active-conflict-awareness',
  standalone: false,
  templateUrl: './active-conflict-awareness.component.html',
  styleUrls: ['./active-conflict-awareness.component.scss'],
})
export class ActiveConflictAwarenessComponent implements OnInit {

  protected gitService = inject(GitService)

  protected settingService = inject(SettingService)

  protected loading = true;

  protected cy: cytoscape.Core | undefined;

  protected selectedCommit?: GitCommitDto;

  protected parentCommits = new Map<string, GitCommitDto>();

  private readonly branchHeadMap = new Map<string, GitBranchDto[]>();

  protected trackingStatusWithReferenceBranchByBranchId = new Map<string, GitBranchTrackingStatusDto>();

  protected selectedCommitsBranches?: GitBranchDto[];

  protected cytoscapeCommits: NodeDefinition[] = []

  protected cytoscapeCommitRelationships: EdgeDefinition[] = []

  private referenceBranchId = "";

  constructor(
          private el: ElementRef,
          private route: ActivatedRoute,
          private router: Router,
          private dialog: MatDialog) {
  }

  async ngOnInit() {
    await this.loadReferenceBranchTrackingStatus();
    await this.fillBranchHeadCommitMap();

    for (const commit of await this.gitService.getCommits()) {
      let color = await this.setCommitBranchLabelAndGetColorOfLabel(commit);
      this.cytoscapeCommits.push({
        data: commit, selectable: true, selected: false, classes: "branch-label", style: {
          "text-background-color": color
        }
      })
      commit.parentIds?.forEach(parentId => this.cytoscapeCommitRelationships.push({
        data: {
          id: commit.id + "-" + parentId, source: parentId, target: commit.id || ""
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
      style: [
        {
          "selector": "node[label]",
          "style": {
            "label": "data(label)"
          }
        },
        {
          "selector": ".branch-label",
          "style": {
            "text-wrap": "wrap",
            "text-background-opacity": 1,
            "color": "#fff",
            "text-background-shape": "roundrectangle",
            "text-border-color": "#000",
            "font-size": "0.75em",
            "text-border-width": 1,
            "text-border-opacity": 1
          }
        }
      ],
      container: this.el.nativeElement.querySelector('#cy'),
      elements: {
        nodes: this.cytoscapeCommits,
        edges: this.cytoscapeCommitRelationships
      },
      layout: {
        name: 'dagre',
        ranker: 'longest-path',
        rankDir: "LR",
        align: "UR",
        nodeDimensionsIncludeLabels: true,
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

  openDiffDialog() {
    this.dialog.open(ActiveConflictAwarenessDiffComponent, {data: this.selectedCommit});
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
      return this.selectedCommitsBranches = branches.sort();
    })
  }

  async loadReferenceBranchTrackingStatus() {
    this.referenceBranchId = await this.settingService.getReferenceBranchId();
    const result = await this.gitService.getBranchTrackingStatusById(this.referenceBranchId)
    result.forEach(ts => {
              if (ts.branchAId === this.referenceBranchId && ts.branchBId) {
                this.trackingStatusWithReferenceBranchByBranchId.set(ts.branchBId, ts);
              } else if (ts.branchBId === this.referenceBranchId && ts.branchAId) {
                this.trackingStatusWithReferenceBranchByBranchId.set(ts.branchAId, ts);
              }
            }
    );
  }

  private async setCommitBranchLabelAndGetColorOfLabel(commit: GitCommitDto): Promise<string> {
    if (this.branchHeadMap.has(commit.id || "")) {
      (commit as any).label = this.branchHeadMap.get(commit.id || "")?.map(x => x.name).join("\n");
      const anyBranchIdOfCommit = this.branchHeadMap.get(commit.id || "")?.at(0)?.id;
      const trackingStatus = this.trackingStatusWithReferenceBranchByBranchId
              .get(anyBranchIdOfCommit || "");
      switch (trackingStatus?.mergeStatus) {
        case "UNKNOWN_MERGE_BASE":
          return '#8a0000';
        case "CONFLICTS":
          return '#ff0000';
        case "TWO_WAY_MERGEABLE":
          return '#afe1af';
        case "THREE_WAY_MERGEABLE":
          return '#50c878';
        default:
          return '#888';
      }
    }
    return '#888';
  }

  private async fillBranchHeadCommitMap() {
    (await this.gitService.getBranches()).forEach(x => {
      const existing = this.branchHeadMap.get(x.headCommitId || "")
      if (existing) {
        existing.push(x)
      } else {
        this.branchHeadMap.set(x.headCommitId || "", [x]);
      }
    });
  }
}
