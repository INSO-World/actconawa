import { Component, ElementRef, inject, OnInit } from '@angular/core';
import { GitBranchDto, GitCommitDto } from '../../../../api';
import cytoscape, { EdgeDefinition, EventObject, NodeDefinition } from 'cytoscape';
import cytoscapeDagre, { DagreLayoutOptions } from 'cytoscape-dagre';
import { GitService } from "../../../services/git.service";

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

  protected selectedCommitsBranches?: GitBranchDto[];

  protected cytoscapeCommits: NodeDefinition[] = []

  protected cytoscapeCommitRelationships: EdgeDefinition[] = []
  constructor(private el: ElementRef) {
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
      if (!commit.id) {
        throw new Error('Commit id must not be null')
      }
      this.selectedCommit = commit;
      this.gitService.getBranchesByCommitId(commit.id).then(branches => {
        return this.selectedCommitsBranches = branches;
      })

    })
  }

  resizeChart() {
    console.log("Resized chart");
    this.cy?.resize();
  }

}
