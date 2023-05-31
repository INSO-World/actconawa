import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import {
  GitBranchControllerService,
  GitBranchDto,
  GitCommitControllerService,
  GitCommitDto,
  GitCommitRelationshipDto
} from '../../../api';
import * as d3 from 'd3';
import { forkJoin } from 'rxjs';
import { graphlib, render } from 'dagre-d3-es';

@Component({
  selector: 'app-active-conflict-awareness',
  templateUrl: './active-conflict-awareness.component.html',
  styleUrls: ['./active-conflict-awareness.component.scss']
})
export class ActiveConflictAwarenessComponent implements OnInit {

  private readonly COMMIT_NODE_ID_PREFIX: string = 'commit-';

  private readonly BRANCH_NODE_ID_PREFIX: string = 'branch-';

  @ViewChild('gitGraph', {static: true}) graphContainer?: ElementRef;

  protected commitsRelationships: GitCommitRelationshipDto[] = [];

  protected nodes?: GitCommitDto[];

  protected links?: GitCommitRelationshipDto[];

  protected branchesByHeadCommitId = new Map<number, GitBranchDto[]>;

  protected branchById = new Map<number, GitBranchDto>;

  protected commits = new Map<number, GitCommitDto>;

  protected loading = true;

  private margin = {top: 10, right: 30, bottom: 30, left: 30};

  protected width = 1920 - (this.margin.left + this.margin.right);

  protected height = 1080 - (this.margin.top + this.margin.bottom);

  protected commitInfo?: string;

  constructor(private gitCommitService: GitCommitControllerService, private gitBranchService: GitBranchControllerService) {
  }

  ngOnInit(): void {
    forkJoin({
              commits: this.gitCommitService.findAllCommits({page: 0, size: 2147483647}),
              branches: this.gitBranchService.findAllBranches({page: 0, size: 2147483647}),
              relationships: this.gitCommitService.findAllCommitRelations({page: 0, size: 2147483647})
            }
    ).subscribe({
      next: (v) => {
        this.commitsRelationships = v?.relationships?.content || [];
        this.nodes = v?.commits?.content;
        v?.commits?.content?.forEach(c => {
          this.commits.set(c.id!!, c)
        })
        this.links = v?.relationships?.content;
        v?.branches.content?.forEach(b => {
          this.branchById.set(b.id!!, b);
          if (this.branchesByHeadCommitId.has(b.headCommitId!!)) {
            this.branchesByHeadCommitId.get(b.headCommitId!!)?.push(b);
          } else {
            this.branchesByHeadCommitId.set(b.headCommitId!!, [b]);
          }
        });
        this.render()
        this.loading = false;
      },
      error: (e) => console.error(e)
    })

  }

  render() {
    const graph = new graphlib.Graph({directed: true});
    graph.setGraph({
      rankdir: 'RL',
      ranksep: 30,
      nodesep: 5
    });
    graph.setDefaultEdgeLabel(() => ({}));

    const svg = d3.select('#git-graph')
    const g = svg.append('g');
    const renderer: any = render();

    this.nodes?.forEach(x => this.setCommit(graph, x))
    this.links?.forEach(x => this.setEdge(graph, x))
    this.nodes?.forEach(x => {
      if (this.branchesByHeadCommitId.has(x.id!!)) {
        this.setBranchLabel(graph, this.branchesByHeadCommitId.get(x.id!!)!!, g);
      }
    })
    renderer(g, graph);

    svg.attr("width", "100%")
    svg.attr("height", "90vh")
    svg.call(d3.zoom<any, any>()
            .scaleExtent([0.1, 8])
            .on('zoom', function ({transform}) {
              g.attr('transform', transform);
            }));
    g.selectAll("g.node").on('mouseover', (event, commitNodeId) => {
      if ((commitNodeId as string).indexOf(this.COMMIT_NODE_ID_PREFIX) === 0) {
        const commitId = (commitNodeId as string).replace(this.COMMIT_NODE_ID_PREFIX, "");
        const commit = this.commits.get(+commitId);
        this.commitInfo = commit!!.sha + "~" + commit!!.message
      }

    })
  }

  private setCommit(graph: graphlib.Graph, gitCommit: GitCommitDto) {
    let color = 'none';
    if (gitCommit.branchIds?.length == 1) {
      const sha = this.commits.get(this.branchById.get(gitCommit.branchIds[ 0 ])!!.headCommitId!!)!!.sha!!;
      color = this.getColorFromShaString(sha);
    }
    graph.setNode(this.COMMIT_NODE_ID_PREFIX + gitCommit.id, {
      radius: 5,
      shape: 'circle',
      style: `stroke: black; fill:${color}; stroke-width: 1px;`
    });
  }

  private setBranchLabel(graph: graphlib.Graph, gitBranches: GitBranchDto[], g: d3.Selection<SVGGElement, unknown, HTMLElement, any>) {
    gitBranches.forEach(branch => {
      graph.setNode(this.BRANCH_NODE_ID_PREFIX + branch.id, {
        label: branch.name,
        height: 5,
        shape: 'rect',
        style: 'stroke: brown; fill:beige; stroke-width: 1px;'
      });
      graph.setEdge(this.COMMIT_NODE_ID_PREFIX + branch.headCommitId + '',
              this.BRANCH_NODE_ID_PREFIX + branch.id,
              {
                lineInterpolate: 'basis',
                style: 'stroke: grey; fill:none; stroke-width: 1px; stroke-dasharray="5,5" d="M5 20 l215 0',
              });
    });

  }

  private setEdge(graph: graphlib.Graph, gitCommitRelation: GitCommitRelationshipDto) {
    graph.setEdge(this.COMMIT_NODE_ID_PREFIX + gitCommitRelation.parentId,
            this.COMMIT_NODE_ID_PREFIX + gitCommitRelation.childId,
            {
              curve: d3.curveBasis,
              style: 'stroke: blue; fill:none; stroke-width: 1px;',
              arrowheadStyle: 'fill: blue'
            });
  }

  private getColorFromShaString(sha: string): string {
    const red = parseInt(sha.substring(0, 2), 16);
    const green = parseInt(sha.substring(2, 4), 16);
    const blue = parseInt(sha.substring(4, 6), 16);
    return `rgb(${red}, ${green}, ${blue})`;

  }
}
