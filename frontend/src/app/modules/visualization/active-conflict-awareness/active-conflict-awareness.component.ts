import { Component, ElementRef, inject, OnInit } from '@angular/core';
import { GitBranchDto, GitBranchTrackingStatusDto, GitCommitDto } from '../../../../api';
import cytoscape, { EdgeDefinition, EventObject, NodeDefinition } from 'cytoscape';
import cytoscapeDagre, { DagreLayoutOptions } from 'cytoscape-dagre';
import cytoscapePopper, { PopperOptions, RefElement } from 'cytoscape-popper';
import { computePosition } from '@floating-ui/dom';
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

  protected drawnCommits = new Set<string>;

  protected missingRelationShipsByCommitId = new Map<string, Set<string>>;

  protected missingRelationShipsByParentCommitId = new Map<string, Set<string>>;

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

    const branchesByHeadCommitId = new Map<string, GitBranchDto[]>;
    const branches = await this.gitService.getBranches();
    branches.forEach(b => {
      const existing = branchesByHeadCommitId.get(b.headCommitId || "");
      if (existing) {
        existing.push(b);
      } else {
        branchesByHeadCommitId.set(b.headCommitId || "", [b]);
      }
    })
    for (const commitId of branchesByHeadCommitId.keys()) {
      this.drawnCommits.add(commitId);
      const commit = (await this.gitService.getCommitAndAncestory(commitId, 0))[ 0 ];
      if (branchesByHeadCommitId.has(commit.id || "")) {
        const cytoscapeCommit: cytoscape.NodeDefinition = {
          data: commit, selectable: true, selected: false, classes: []
        }
        this.cytoscapeCommits.push(cytoscapeCommit)
        commit.parentIds?.forEach(parentId => {
          if (branchesByHeadCommitId.has(parentId || "")) {
            this.cytoscapeCommitRelationships.push({
              data: {
                id: commit.id + "-" + parentId, source: parentId, target: commit.id || ""
              }
            });
          } else {
            this.addMissingCommitRelationship(commit.id || "", parentId)
          }
        })
        if (this.missingRelationShipsByCommitId.has(commit.id || "")) {
          (cytoscapeCommit.classes as string[]).push("commit-placeholder");
        }
      }
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
    cytoscape.use(cytoscapePopper(this.popperFactory));
    this.cy = cytoscape({
      container: this.el.nativeElement.querySelector('#cy'),
      elements: {
        nodes: this.cytoscapeCommits,
        edges: this.cytoscapeCommitRelationships
      },
      style: [
        {
          selector: ".commit-dependency",
          style: {
            "border-color": "black",
            "border-width": "5px"
          }
        },
        {
          selector: ".commit-placeholder",
          style: {
            "shape": "diamond",
            "font-weight": "bold",
            "content": "+",
            "text-valign": "center",
            "text-halign": "center"
          }
        }
      ],
      layout: {
        name: 'concentric',
        concentric: function (node) {
          return (node as any).data().id.startsWith("ph") ? 1 : 3;
        },
        levelWidth: function (nodes) { // the variation of concentric values in each level
          return 1;
        },
        fit: true,
        nodeDimensionsIncludeLabels: true,
        minNodeSpacing: 10,
        spacingFactor: 4
      }
    });
    this.loading = false;
    this.cy.on('click', 'node', (e: EventObject) => {
      this.selectedCommitsBranches = undefined
      const commit = e.target._private.data as GitCommitDto;
      if (!this.missingRelationShipsByCommitId.has(commit.id || "")) {
        this.selectCommit(commit, false);
      } else {
        const parentIdsToBeLoaded = this.missingRelationShipsByCommitId.get(commit.id || "") || []

        this.missingRelationShipsByCommitId.delete(commit.id || "");
        this.cy?.$('#' + commit.id).removeClass("commit-placeholder")

        for (let missingParentId of parentIdsToBeLoaded) {
          this.gitService.getCommitAndAncestory(missingParentId).then(loadedAncestors => {

            // Retrieve new nodes
            const newNodes: NodeDefinition[] = loadedAncestors.map(c => {
              this.drawnCommits.add(c.id || "")
              return {
                data: c,
                selectable: true,
                selected: false,
                classes: []
              }
            });
            // Declare new node as one that has missing ancestors
            newNodes.forEach(newNode => {
              ((newNode.data as GitCommitDto).parentIds || []).forEach(newNodeParentId => {
                if (!this.drawnCommits.has(newNodeParentId)) {
                  this.addMissingCommitRelationship(newNode.data.id || "", newNodeParentId);
                  const classes = newNode.classes as string[];
                  if (classes.indexOf("commit-placeholder") < 0) {
                    classes.push("commit-placeholder")
                  }
                }
              })
            })
            // retrieve all new edges for all new nodes (not existing ones)
            let newEdges: EdgeDefinition[] = loadedAncestors.flatMap(c => (c.parentIds || [])
                    .filter(pid => this.drawnCommits.has(pid))
                    .map(pid => {
                              return ({
                                data: {
                                  id: c.id + "-" + pid,
                                  source: pid,
                                  target: c.id || ""
                                }
                              } as EdgeDefinition);
                            }
                    ) || []
            );
            // retrieve all new edges for all existing nodes
            for (let newAddedCommitNode of newNodes) {
              for (let childCommitOfLoadedParent of
              this.missingRelationShipsByParentCommitId.get(newAddedCommitNode.data.id || "") || []) {
                newEdges.push({
                  data: {
                    id: childCommitOfLoadedParent + "-" + newAddedCommitNode.data.id || "",
                    source: newAddedCommitNode.data.id || "",
                    target: childCommitOfLoadedParent || ""
                  }
                });
                this.missingRelationShipsByCommitId.get(childCommitOfLoadedParent)?.delete(newAddedCommitNode.data.id
                        || "");
                this.cy?.$('#' + childCommitOfLoadedParent).removeClass("commit-placeholder")
              }
              this.missingRelationShipsByParentCommitId.delete(newAddedCommitNode.data.id || "")
            }

            // Add nodes/edges to graph and apply layout
            this.cy?.add({
              nodes: newNodes,
              edges: newEdges
            })
            this.cy?.layout({
              name: 'dagre',
              rankDir: "LR",
              rankSep: 130,
              nodeDimensionsIncludeLabels: true,
              spacingFactor: 1.75,
              fit: true,
              animate: true,
              animationDuration: 500,
              ready: e1 => {
                this.gitService.getCommitById(commit.id || "")
                        .then(commitToSelect => {
                          if (commitToSelect) {
                            this.selectCommit(commitToSelect, true);
                          }
                        })

              }
            } as DagreLayoutOptions).run();
          })
        }

      }
    })

    for (let branchHeadAtCommit of branchesByHeadCommitId.values()) {
      const branchHead = this.cy.$('#' + branchHeadAtCommit[ 0 ].headCommitId);
      let branchHeadPopper = branchHead.popper({
        content: () => {
          let div = document.createElement('div');

          // Taking first one is sufficient
          const trackingStatus =
                  this.trackingStatusWithReferenceBranchByBranchId.get(branchHeadAtCommit[ 0 ].id || "");
          let branchTags = "";
          for (let branch of branchHeadAtCommit) {
            branchTags += `<div class="popper-branch-tag ${trackingStatus?.mergeStatus}">${branch.name}</div>`
          }
          if (!trackingStatus) {
            div.innerHTML = `${branchTags}
                           <span>Reference Branch</span>
                          `;
          } else {
            div.innerHTML = `${branchTags}
                           <span>${trackingStatus?.behindCount} Behind / ${trackingStatus?.aheadCount} Ahead</span><br>
                           <span>${trackingStatus?.conflictingFilePaths?.length} Conflicting Files</span>
                          `;
          }
          div.classList.add('popper-div');
          this.el.nativeElement.querySelector('#cy').appendChild(div);
          return div;
        },
        popper: {
          placement: 'bottom',
        },
      });
      this.cy.on('pan zoom resize drag', (e: EventObject) => {
        (branchHeadPopper as any).update(this.cy?.zoom());
      });
      branchHead.on('position', (branchHeadPopper as any).update());
    }

  }

  resizeChart() {
    this.cy?.resize();
  }

  addMissingCommitRelationship(commitId: string, parentId: string) {
    if (!this.missingRelationShipsByCommitId.has(commitId)) {
      this.missingRelationShipsByCommitId.set(commitId, new Set<string>());
    }
    (this.missingRelationShipsByCommitId.get(commitId) || new Set<string>()).add(parentId)

    if (!this.missingRelationShipsByParentCommitId.has(parentId)) {
      this.missingRelationShipsByParentCommitId.set(parentId, new Set<string>());
    }
    (this.missingRelationShipsByParentCommitId.get(parentId) || new Set<string>()).add(commitId)

  }

  openDiffDialog() {
    this.dialog.open(ActiveConflictAwarenessDiffComponent, {
      data: this.selectedCommit,
      hasBackdrop: true,
      panelClass: "dialog-100vw"
    });
  }

  popperFactory(ref: RefElement, content: HTMLElement, options?: PopperOptions): any {
    const popperOptions = {
      ...options,
    }

    function update(zoom?: number) {
      computePosition(ref, content, popperOptions).then(({x, y}) => {
        Object.assign(content.style, {
          'font-size': (zoom || 1) + "rem",
          'line-height': (zoom || 1) + "rem",
          left: `${x}px`,
          top: `${y}px`,
        });
      });
    }

    update();
    return {update};
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

    this.loadDependencies(commit).then(() => {
    });
  }

  private async loadReferenceBranchTrackingStatus() {
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

  private async loadDependencies(commit: GitCommitDto) {
    this.cy?.$("node").removeClass("commit-dependency");
    for (const parentId of commit.parentIds || "") {
      const files =
              await this.gitService.getModifiedFilesByCommitIds(commit.id || "", parentId) || [];
      for (const file of files) {
        const hunks = await this.gitService.getHunksByDiffFileId(file.id || "") || [];
        for (const hunk of hunks) {
          const dependencyCommitIds = hunk.commitDependencyIds || [];
          for (const dep of dependencyCommitIds) {
            if (dep && dep.length > 0) {
              console.log(dep)
              this.cy?.$('#' + dep).addClass("commit-dependency");
            }
          }
        }
      }
    }
  }
}
