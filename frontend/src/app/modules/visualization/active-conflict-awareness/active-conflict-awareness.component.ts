import { Component, ElementRef, inject, OnInit } from '@angular/core';
import { GitBranchDto, GitBranchTrackingStatusDto, GitCommitDto } from '../../../../api';
import cytoscape, { EdgeDefinition, EventObject, NodeDefinition, NodeSingular } from 'cytoscape';
import cytoscapeDagre, { DagreLayoutOptions } from 'cytoscape-dagre';
import cytoscapePopper, { PopperOptions, RefElement } from 'cytoscape-popper';
import expandCollapse from 'cytoscape-expand-collapse'
import { computePosition } from '@floating-ui/dom';
import { GitService } from "../../../services/git.service";
import { ActivatedRoute, Router } from "@angular/router";
import { SettingService } from "../../../services/setting.service";
import { MatDialog } from "@angular/material/dialog";
import {
  ActiveConflictAwarenessDiffComponent
} from "./active-conflict-awareness-diff/active-conflict-awareness-diff.component";
import { ExtendedGitCommitDto } from "../../../utils/ExtendedGitCommitDto";
import ExpandCollapseAPI = cytoscape.ExpandCollapseAPI;

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

  protected ec: ExpandCollapseAPI | undefined;

  protected selectedCommit?: GitCommitDto;

  protected selectedBranchHeads: GitBranchDto[] = [];

  protected parentCommits = new Map<string, GitCommitDto>();

  protected popperDivsByNodeId = new Map<string, HTMLElement>();

  protected popperDivsInMainCollapse: HTMLElement[] = [];

  private readonly mainCollapseId = "main-collapse";

  private mainCollapseSelected: boolean = false;

  private readonly branchHeadMap = new Map<string, GitBranchDto[]>();

  private readonly branchHeadStatusMap = new Map<string, GitBranchTrackingStatusDto[]>();

  protected trackingStatusWithReferenceBranchByBranchId = new Map<string, GitBranchTrackingStatusDto>();

  protected selectedCommitsBranches?: GitBranchDto[];

  protected cytoscapeCommits: NodeDefinition[] = []

  protected cytoscapeCommitRelationships: EdgeDefinition[] = []

  private referenceBranchId = "";

  private defaultLayout = {
    name: 'dagre',
    rankDir: "LR",
    rankSep: 130,
    ranker: "network-simplex",
    nodeDimensionsIncludeLabels: true,
    spacingFactor: 1.75,
    fit: true,
    animate: true,
    animationDuration: 500,
  } as DagreLayoutOptions;

  constructor(
          private el: ElementRef,
          private route: ActivatedRoute,
          private router: Router,
          private dialog: MatDialog) {
  }

  async ngOnInit() {
    await this.fillBranchHeadCommitMap();
    await this.loadBranchTrackingStatus();

    (await this.gitService.getCommitGroups()).forEach(group => {
      this.cytoscapeCommits.push({data: group, selectable: false, selected: false} as NodeDefinition);
    });

    for (const commit of await this.gitService.getCommits()) {
      const node = {
        data: commit as ExtendedGitCommitDto, selectable: true, selected: false
      };
      node.data.parent = node.data.groupId;
      this.cytoscapeCommits.push(node)
      commit.parentIds?.forEach(parentId => this.cytoscapeCommitRelationships.push({
        data: {
          id: commit.id + "-" + parentId, source: parentId, target: commit.id || ""
        }
      }))
    }

    // preselect commit if provided in the route.
    const presetCommitId = this.route.snapshot.queryParamMap.get('commitId');
    if (presetCommitId) {
      this.gitService.getCommitById(presetCommitId).then(presetCommit => {
        if (presetCommit) {
          this.selectCommit(presetCommit, true);
        }
      });
    }

    // initialize graph
    cytoscape.use(cytoscapeDagre);
    cytoscape.use(cytoscapePopper(this.popperFactory));
    cytoscape.use(expandCollapse);

    this.cy = cytoscape({
      container: this.el.nativeElement.querySelector('#cy'),
      elements: {
        nodes: this.cytoscapeCommits,
        edges: this.cytoscapeCommitRelationships
      },
      style: [
        {
          selector: "node.cy-expand-collapse-collapsed-node",
          css: {
            "background-color": "dodgerblue",
            "shape": "diamond",
            "font-weight": "bold",
            "color": "white",
            "content": "+",
            "text-valign": "center",
            "text-halign": "center"
          }
        },
        {
          selector: ".commit-dependency",
          css: {
            "border-color": "black",
            "border-width": "3px"
          }
        },
        {
          selector: ".selected-branch-exlusive-commits",
          css: {
            "background-color": "lightgreen"
          }
        },
        {
          selector: ".conflicting-branch-exlusive-commits",
          css: {
            "background-color": "lightcoral",
          }
        },
        {
          selector: ":selected",
          css: {
            "border-color": "blue",
            "border-width": "7px",
            "shape": "star",
          }
        },
        {
          selector: ':parent',
          css: {
            "text-valign": "bottom",
            "text-halign": "center",
            "shape": "round-rectangle"
          }
        }
      ],
      layout: this.defaultLayout
    });

    this.ec = this.cy.expandCollapse({
      layoutBy: this.defaultLayout,
      fisheye: false,
      animate: true,
      undoable: true,
    });
    this.ec.collapseAll();
    this.ec.collapseAllEdges();
    this.loading = false;
    this.cy.on('click', 'node', (e: EventObject) => {
      // Cleanup context specific classes that might change
      this.cy?.nodes().removeClass(
              "selected-branch-exlusive-commits conflicting-branch-exlusive-commits commit-dependency")
      if (!e.target._private.selectable) {
        if (this.ec?.isCollapsible(e.target)) {
          this.ec?.collapseEdges(e.target);
          this.ec?.collapse(e.target);
        } else {
          this.ec?.expandEdges(e.target);
          this.ec?.expand(e.target);
        }
        // Workaround to several renderingbugs. Even if classes were set correctly in collapsed nodes,
        // they were not rendered correctly (only on certain zoom levels).
        // Therefore the logic to set the classes was removed and a shortcut to simply reselect the node
        // was added. When no node is selected, anyway there is no context to be shown.
        this.cy?.nodes().removeClass([
          "selected-branch-exlusive-commits",
          "conflicting-branch-exlusive-commits",
          "commit-dependency"
        ].join(" "))
        if (this.selectedCommit) {
          this.selectCommit(this.selectedCommit, false)
        }

        return;
      }
      this.selectedCommitsBranches = undefined
      const commit = e.target._private.data as GitCommitDto;
      this.selectCommit(commit, false);
    })
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

    // Add branch labels/tags to graph with popper
    for (let branchHeadAtCommit of branchesByHeadCommitId.values()) {
      const branchHead = this.cy.$('#' + branchHeadAtCommit[ 0 ].headCommitId);
      let branchHeadPopper = branchHead.popper({
        content: () => {
          let div = document.createElement('div');

          // Taking first one is sufficient
          const trackingStatus =
                  this.trackingStatusWithReferenceBranchByBranchId.get(branchHeadAtCommit[ 0 ].id || "");
          let branchTags = "";
          let statusIcon = "";
          if (trackingStatus) {
            switch (trackingStatus.mergeStatus) {
              case "MERGED":
                statusIcon = `<i class='bx bx-git-merge'></i><i class='bx bxs-check-circle'></i>`;
                break;
              case "THREE_WAY_MERGEABLE":
              case "TWO_WAY_MERGEABLE":
                statusIcon = `<i class='bx bx-git-merge'></i><i class='bx bxs-rocket' ></i>`
                break;
              case "CONFLICTS":
                statusIcon = `<i class='bx bx-git-merge'></i><i class='bx bxs-error-alt'></i>`;
                break;
              default:
                statusIcon = `<i class='bx bx-git-merge'></i><i class='bx bxs-help-circle'></i>`;

            }
          }
          for (let branch of branchHeadAtCommit) {
            branchTags
                    += `<div class="popper-branch-tag ${trackingStatus?.mergeStatus}">${branch.name}${statusIcon}</div>`
          }
          if (!trackingStatus) {
            div.innerHTML = `${branchTags}
                           <span class="reference-hint">Selected reference</span>
                          `;
          } else if (trackingStatus.mergeStatus === "MERGED") {
            div.innerHTML = `${branchTags}`;
          } else {
            const status = this.branchHeadStatusMap.get(trackingStatus.branchBId || "") || []
            const conflicting = status.filter(x => x.mergeStatus === "CONFLICTS").length
            const unknownMergeBase = status.filter(x => x.mergeStatus === "UNKNOWN_MERGE_BASE").length
            const mergable = status.filter(x => x.mergeStatus
                    === "TWO_WAY_MERGEABLE"
                    || x.mergeStatus
                    === "THREE_WAY_MERGEABLE").length;
            let otherBranchTracking = "";
            if (mergable > 0) {
              otherBranchTracking += `
                 <span class="count-label mergeable">
                 <i class='bx bx-git-pull-request' ></i><i class='bx bx-check-circle' ></i>
                 ${mergable}
                 </span>
              `;
            }
            if (conflicting > 0) {
              otherBranchTracking += `
                 <span class="count-label conflicts">
                 <i class='bx bx-git-pull-request' ></i><i class='bx bx-x-circle' ></i>
                 ${conflicting}
                 </span>
              `;
            }
            if (unknownMergeBase > 0) {
              otherBranchTracking += `
                 <span class="count-label unknown-merge-base">
                 <i class='bx bx-git-pull-request' ></i><i class='bx bx-help-circle' ></i>
                 ${unknownMergeBase}
                 </span>
              `;
            }

            div.innerHTML = `${branchTags}
                           <div class="reference-branch-infos">
                           <div class="description-header">Reference Branch</div>
                           <span class="behind-tag"><i class='bx bx-git-commit' ></i><i class='bx bx-minus-circle'></i>${trackingStatus?.behindCount}</span>
                           <span class="ahead-tag"><i class='bx bx-git-commit' ></i><i class='bx bx-plus-circle'></i>${trackingStatus?.aheadCount}</span>
                           <span class="behind-ahead-desc">Commits</span>
                           <span class="conflicts-tag"><i class='bx bx-file-blank'></i><i class='bx bxs-zap' ></i>${trackingStatus?.conflictingFilePaths?.length}</span>
                           <span class="conflicts-desc"> Files Conflicting</span>
                           </div>
                           <div class="other-branch-infos">
                           <div class="description-header">Other Branches</div>
                           ${otherBranchTracking}
                           </div>
                          `;

          }
          div.classList.add('popper-div');
          if (branchHeadAtCommit[ 0 ].headCommitId) {
            this.popperDivsByNodeId.set(branchHeadAtCommit[ 0 ].headCommitId, div);
          }
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
    if (!this.mainCollapseSelected) {
      const predecessors = this.cy.$('#'
              + (await this.gitService.getBranchById(this.referenceBranchId))?.headCommitId).predecessors().nodes();
      // rather "dirty" solution as the order of predecessor is undocumented...
      const selectedMainCollapse = 10 < predecessors.size()
              ? (predecessors as any)[ 10 ]
              : (predecessors as any)[ predecessors.size() - 1 ]
      this.mainCollapseSelected = true;
      this.mainCollapse((selectedMainCollapse as NodeSingular).id())
    }

  }

  resizeChart() {
    this.cy?.resize();
    this.cy?.layout(this.defaultLayout)
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

    this.selectedBranchHeads = this.branchHeadMap.get(commit.id) || [];

    this.loadDependencies(commit).then(() => {
    });

    if (this.selectedBranchHeads.length > 0) {
      this.markConflictingBranchCommits(this.selectedBranchHeads[ 0 ])
    }

  }

  private mainCollapse(commitId: string) {
    this.cy?.add({data: {id: this.mainCollapseId}, selectable: false, selected: false} as NodeDefinition)
            .on('expandcollapse.beforecollapse', () => {
              this.hidePopper()
            })
            .on('expandcollapse.afterexpand', () => {
              this.showHiddenPopper()
            });
    this.cy?.$('#' + commitId)
            .successors()
            .absoluteComplement()
            .nodes().forEach(node => {
      if (node.id() !== this.mainCollapseId && !node.data().parent) {
        node.move({parent: this.mainCollapseId});
        const popper = this.popperDivsByNodeId.get(node.id());
        if (popper) {
          this.popperDivsInMainCollapse.push(popper)
        }
      }
      return true;
    });
    const mainCollapseNode = this.cy?.$('#' + this.mainCollapseId);
    if (mainCollapseNode) {
      this.ec?.collapse(mainCollapseNode);
    }
    this.cy?.layout(this.defaultLayout).run();
  }

  private hidePopper() {
    this.popperDivsInMainCollapse.forEach(x => x.classList.add("hidden"))
  }

  private showHiddenPopper() {
    this.popperDivsInMainCollapse.forEach(x => x.classList.remove("hidden"))
  }

  private async loadBranchTrackingStatus() {
    this.referenceBranchId = await this.settingService.getReferenceBranchId();
    const result = await this.gitService.getBranchTrackingStatusById(this.referenceBranchId)

    result.forEach(ts => {
              if (ts.branchAId === this.referenceBranchId && ts.branchBId) {
                this.trackingStatusWithReferenceBranchByBranchId.set(ts.branchBId, ts);
              }
            }
    );

    const unmergedBranchIds = result
            .filter(x => x.mergeStatus && x.mergeStatus != "MERGED")
            .map(x => x.branchBId);

    const trackingAvailableBranchIds = result.map(x => x.branchBId);

    for (const branchA of trackingAvailableBranchIds) {
      for (const branchB of trackingAvailableBranchIds) {
        if (!branchA || !branchB || branchA === branchB) {
          continue;
        }
        const status = await this.gitService.getBranchTrackingStatusByIds(branchA, branchB);
        if (!this.branchHeadStatusMap.has(branchA)) {
          this.branchHeadStatusMap.set(branchA, [])
        }
        if (status) {
          this.branchHeadStatusMap.get(branchA)?.push(status);
        }
      }
    }
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
              this.cy?.$('#' + dep).addClass("commit-dependency");
              const commit = await this.gitService.getCommitById(dep);
              this.cy?.$('#' + commit?.groupId).addClass("commit-dependency");
            }
          }
        }
      }
    }
  }

  private async markConflictingBranchCommits(branch: GitBranchDto) {
    if (branch.id! == this.referenceBranchId) {
      return;
    }
    const status = this.branchHeadStatusMap.get(branch.id!)

    if (!status) {
      throw new Error("Branch head must have a status. Maybe index bug.");
    }

    const otherBranchIds = status.filter(s => s.mergeStatus === "CONFLICTS")
            .map(s => s.branchBId!);

    const commit1 = this.cy?.$("#" + branch.headCommitId);
    const commit1Ancestry = commit1?.predecessors().union(commit1);

    for (const otherBranchId of otherBranchIds) {
      const otherBranch = await this.gitService.getBranchById(otherBranchId);
      if (!otherBranch) {
        throw new Error("BranchId must have a corresponding branch.");
      }
      const commit2 = this.cy?.$("#" + otherBranch.headCommitId);
      const commit2Ancestry = commit2?.predecessors().union(commit2);

      if (commit1Ancestry && commit2Ancestry) {
        const ancestryDiff = commit1Ancestry?.symmetricDifference(commit2Ancestry!);
        const commit1ExclusiveAncestry = commit1Ancestry.intersection(ancestryDiff).nodes();
        const commit2ExclusiveAncestry = commit2Ancestry.intersection(ancestryDiff).nodes();

        const result1CollapsedIds = this.ec
                ?.getCollapsedChildren(commit1ExclusiveAncestry)?.nodes();
        const result1Ids = result1CollapsedIds ?
                result1CollapsedIds.union(commit1ExclusiveAncestry) : commit1ExclusiveAncestry;
        const result2CollapsedIds = this.ec
                ?.getCollapsedChildren(commit2ExclusiveAncestry)?.nodes();
        const result2Ids = result2CollapsedIds ?
                result2CollapsedIds.union(commit2ExclusiveAncestry) : commit2ExclusiveAncestry;
        commit1ExclusiveAncestry?.nodes().addClass("selected-branch-exlusive-commits");
        commit2ExclusiveAncestry?.nodes().addClass("conflicting-branch-exlusive-commits");

      }
    }
  }

}
