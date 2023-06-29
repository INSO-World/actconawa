import { Component, OnInit, ViewChild } from '@angular/core';
import {
  GitBranchControllerService,
  GitBranchDto,
  GitCommitControllerService,
  GitCommitDto
} from '../../../api';
import * as d3 from 'd3';
import { json } from 'd3';
import { graphlib, render } from 'dagre-d3-es';
import { MatTooltip } from "@angular/material/tooltip";

@Component({
  selector: 'app-active-conflict-awareness',
  templateUrl: './active-conflict-awareness.component.html',
  styleUrls: ['./active-conflict-awareness.component.scss']
})
export class ActiveConflictAwarenessComponent implements OnInit {

  private readonly COMMIT_NODE_ID_PREFIX: string = 'commit-';

  private readonly BRANCH_NODE_ID_PREFIX: string = 'branch-';

  @ViewChild('commitInfoTooltip', {static: true}) commitInfoTooltip?: MatTooltip;

  protected nodes: GitCommitDto[] = [];

  protected branchesByHeadCommitId = new Map<string, GitBranchDto[]>;

  protected branchById = new Map<string, GitBranchDto>;

  protected commits = new Map<string, GitCommitDto>;

  protected edgeFlag: Set<string> = new Set<string>();

  protected branches: GitBranchDto[] = [];

  protected loading = true;

  private margin = {top: 10, right: 30, bottom: 30, left: 30};

  protected width = 1920 - (this.margin.left + this.margin.right);

  protected height = 1080 - (this.margin.top + this.margin.bottom);

  protected commitInfo?: string;

  protected selectedCommit?: GitCommitDto;

  private graph: graphlib.Graph;

  constructor(private gitCommitService: GitCommitControllerService, private gitBranchService: GitBranchControllerService) {
    this.graph = new graphlib.Graph({directed: true});
    this.configureGraph();
  }

  ngOnInit(): void {
    this.render();
    this.gitBranchService.findAllBranches({page: 0, size: 2147483647}).subscribe(
            {
              next: branches => {
                this.branches = branches.content || [];
                branches.content?.forEach(branch => {
                  if (branch.headCommitId) {
                    console.log(branch.name + " -- " + branch.headCommitId)
                    const headCommit = branch.headCommitId;
                    if (!this.commits.has(branch.headCommitId!)) {
                      this.fetchAndAddAncestors(headCommit);
                    }
                  }
                  branches.content?.forEach(b => {
                    this.branchById.set(b.id!, b);
                    if (this.branchesByHeadCommitId.has(b.headCommitId!)) {
                      this.branchesByHeadCommitId.get(b.headCommitId!)?.push(b);
                    } else {
                      this.branchesByHeadCommitId.set(b.headCommitId!, [b]);
                    }
                  });
                  this.loading = false;
                })
              },
              error: e => {
              }
            }
    )

  }

  configureGraph() {
    this.graph.setGraph({
      directed: true,
      rankdir: 'RL',
      align: 'UL',
      ranksep: 100,
      nodesep: 100,
      ranker: 'tight-tree',
      animate: 1000,

    });
    this.graph.setDefaultEdgeLabel(() => ({}));
  }

  /**
   * Retrieve ancestor commits of a commit.
   * Returns the commits where further fetches are necessary
   * @param commitId
   * @param previousChildCommitId
   */
  fetchAndAddAncestors(commitId: string, previousChildCommitId?: string): GitCommitDto[] {
    this.gitCommitService.findAncestors(commitId, 1).subscribe({
      next: ancestors => {
        for (let i = 0; i < ancestors.length; i++) {
          const c = ancestors[ i ];
          if (!this.commits.has(c.id!)) {
            this.nodes?.push(c);
            this.commits.set(c.id!, c);
            this.setCommit(c);
            if (c.parentIds
                    && c.parentIds?.length
                    && c.parentIds.length
                    > 1
                    || ancestors.length
                    == i
                    + 1
                    && c.parentIds) {
              c.parentIds.forEach(parentId => {
                this.fetchAndAddAncestors(parentId, c.id!!)
              })
            }
          }
          if (i == 0 && previousChildCommitId) {
            this.setEdge(c.id!!, previousChildCommitId)
          } else if (i > 0) {
            this.setEdge(c.id!!, ancestors[ i - 1 ].id!!)
          }
          const g = d3.select('#git-graph-group') as d3.Selection<SVGGElement, unknown, HTMLElement, any>;
          this.renderGraph(g);
        }

      },
      error: err => {
      }

    })
    return [];

  }

  render() {
    const svg = d3.select('#git-graph')
            .attr('width', '100%')
            .attr('height', '90vh');
    const g = svg.append('g').attr('id', 'git-graph-group');
    const tooltipRef = this.commitInfoTooltip;

    this.renderGraph(g);

    const graph = this.graph;
    const zoomFunction = d3.zoom<any, any>()
            .scaleExtent([0.1, 8])
            .on('zoom', function ({transform}) {
              g.attr('transform', transform);
              tooltipRef?.hide(0);
            });

    svg.call(zoomFunction);
    /*          .call(zoomFunction.transform,
                      d3.zoomIdentity
                              .translate(window.innerWidth < graph.graph().width
                                              ? 0
                                              : (window.innerWidth - graph.graph().width) / 2,
                                      window.innerHeight / 3)
              );

   */
  }

  private configureCommitOnClick(g: d3.Selection<SVGGElement, unknown, HTMLElement, any>) {
    g.selectAll('g.commit-node').on('click', (event, commitNodeId) => {
      console.log("clicked " + commitNodeId)
      const commitId = (commitNodeId as string).replace(this.COMMIT_NODE_ID_PREFIX, '');
      this.selectedCommit = this.commits.get(commitId);
      event.stopPropagation();
    });
  }

  private configureCommitInfoTooltip(g: d3.Selection<SVGGElement, unknown, HTMLElement, any>) {
    g.selectAll('g.node').on('mouseenter', (event, commitNodeId) => {
      console.log("menter " + commitNodeId)
      // make sure previous tooltip is not reused including the origin
      this.commitInfoTooltip?.ngOnDestroy();
      if ((commitNodeId as string).indexOf(this.COMMIT_NODE_ID_PREFIX) === 0) {
        if (this.commitInfoTooltip?._isTooltipVisible()) {
          this.commitInfoTooltip?.hide();
        } else {
          this.commitInfoTooltip?.show(this.commitInfoTooltip?.showDelay,
                  {"x": event.x, "y": event.y});
        }
        const commitId = (commitNodeId as string).replace(this.COMMIT_NODE_ID_PREFIX, '');
        const commit = this.commits.get(commitId);
        this.commitInfo = commit!.sha!.substring(0, 8)
                + ' | ' + commit!.message;
      }
    })
    g.selectAll('g.node').on('mouseout', () => {
      this.commitInfoTooltip?.hide(this.commitInfoTooltip?.hideDelay);
    })
  }

  private renderGraph(g: d3.Selection<SVGGElement, unknown, HTMLElement, any>) {
    // fix branch labels by removing and readding them
    this.drawBranchLabel();
    const renderer = render();
    renderer(g, this.graph);
    this.configureCommitOnClick(g);
    this.configureCommitInfoTooltip(g);
  }

  private drawBranchLabel() {
    const g = d3.select('#git-graph-group') as d3.Selection<SVGGElement, unknown, HTMLElement, any>;
    g?.select('.branch-label').remove();
    const branchLabelG = g.select('.output')
            .append('g')
            .attr('class', 'branch-label') as d3.Selection<SVGGElement, unknown, HTMLElement, any>;
    // TODO
    let offset = 0;
    this.branches.forEach(branch => {
      const commitNode = g.select('#' + this.COMMIT_NODE_ID_PREFIX + branch.headCommitId);
      if (commitNode && commitNode.node()) {
        const commitNodeBBox = (commitNode.node()! as SVGSVGElement).getBoundingClientRect();
        console.log((commitNode.node() as HTMLElement).attributes.getNamedItem("transform")?.textContent)
        const branchLabelPadding = 10;
        const branchLabelHeight = 20;
        const branchLabel = branchLabelG.append('g')
                .attr('class', 'branch-label')
                .attr('id', this.BRANCH_NODE_ID_PREFIX + branch.id);

        const branchLabelContainer = branchLabel.append('rect')
                .attr('class', 'branch-label-container')
                .attr('height', branchLabelHeight)
                .attr('rx', 2)
                .attr('ry', 2)
                .style('fill', 'beige')
                .style('stroke', 'brown')

        const label = branchLabel.append('g')
                .attr('class', 'label')
                .append('text')
                .append('tspan')
                .attr('space', 'preserve')
                .attr('dy', '1em')
                .attr('x', branchLabelPadding)
                .text(branch.name + '')

        // Calculate width of the label text
        const textWidth = label.node()?.getComputedTextLength() || 0;
        // Set width of branch label container
        branchLabelContainer.attr('width', textWidth + branchLabelPadding * 2);
        branchLabelContainer.attr('transform', textWidth + branchLabelPadding * 2);
        // Position the branchLabel
        const x = 20;
        const y = 0;
        offset += branchLabelHeight;
        branchLabel.attr('transform', `translate(${x},${y})`);
        branchLabel.attr('transform',
                (commitNode.node() as HTMLElement).attributes.getNamedItem("transform")?.textContent
                + "");
      } else {
        console.log("cannot find commit node "
                + '#'
                + this.COMMIT_NODE_ID_PREFIX
                + branch.headCommitId
                + "to add branchlabel")
      }
    });
  }

  private setCommit(gitCommit: GitCommitDto) {
    // must have a color, never none, otherwise click and hover handling chooses svg
    let color = 'white';
    if (gitCommit.branchIds?.length == 1) {
      const sha = this.commits.get(this.branchById.get(gitCommit.branchIds[ 0 ])!.headCommitId!)!.sha!;
      color = this.getColorFromShaString(sha);
    }
    this.graph.setNode(this.COMMIT_NODE_ID_PREFIX + gitCommit.id, {
      id: this.COMMIT_NODE_ID_PREFIX + gitCommit.id,
      class: 'commit-node',
      label: '  ',
      radius: 5, // TODO: seems to have no effect
      shape: 'circle',
      style: `stroke: black; fill:${color}; stroke-width: 1px;`
    });
  }

  private setEdge(parentId: string, childId: string) {
    if (!this.edgeFlag.has(parentId + childId)) {
      this.edgeFlag.add(parentId + childId);
      this.graph.setEdge(this.COMMIT_NODE_ID_PREFIX + parentId,
              this.COMMIT_NODE_ID_PREFIX + childId,
              {
                curve: d3.curveBasis,
                style: 'stroke: blue; fill:none; stroke-width: 1px;',
                arrowheadStyle: 'fill: blue'
              });
    }
  }

  private getColorFromShaString(sha: string): string {
    const red = parseInt(sha.substring(0, 2), 16);
    const green = parseInt(sha.substring(2, 4), 16);
    const blue = parseInt(sha.substring(4, 6), 16);
    return `rgb(${red}, ${green}, ${blue})`;

  }

  protected readonly String = String;

  protected readonly json = json;
}
