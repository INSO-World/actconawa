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
  @ViewChild('gitGraph', {static: true}) graphContainer?: ElementRef;

  protected commitsRelationships: GitCommitRelationshipDto[] = [];

  protected nodes?: GitCommitDto[];

  protected links?: GitCommitRelationshipDto[];

  protected branches?: GitBranchDto[];

  protected loading = true;

  private margin = {top: 10, right: 30, bottom: 30, left: 30};

  protected width = 1920 - (this.margin.left + this.margin.right);

  protected height = 1080 - (this.margin.top + this.margin.bottom);

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
        this.links = v?.relationships?.content;
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

    this.nodes?.forEach(x => this.setCommit(graph, x))
    this.links?.forEach(x => this.setEdge(graph, x))

    const svg = d3.select('#git-graph')
    const g = svg.append('g');
    const renderer: any = render();
    renderer(g, graph);

    svg.attr("width", "100%")
    svg.attr("height", "95vh")
    svg.call(d3.zoom<any, any>()
            .scaleExtent([0.1, 8])
            .on('zoom', function ({transform}) {
              g.attr('transform', transform);
            }));
  }

  private setCommit(graph: graphlib.Graph, gitCommit: GitCommitDto) {
    graph.setNode(gitCommit.id + '', {
      radius: 5,
      shape: 'circle',
      style: 'stroke: black; fill:none; stroke-width: 1px;'
    });
  }

  private setEdge(graph: graphlib.Graph, gitCommitRelation: GitCommitRelationshipDto) {
    graph.setEdge(gitCommitRelation.parentId + '', gitCommitRelation.childId + '', {
      curve: d3.curveBasis,
      style: 'stroke: blue; fill:none; stroke-width: 1px;',
      arrowheadStyle: 'fill: blue'
    });
  }
}
