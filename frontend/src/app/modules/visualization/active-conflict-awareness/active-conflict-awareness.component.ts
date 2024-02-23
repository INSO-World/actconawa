import { Component, ElementRef, inject, OnInit } from '@angular/core';
import { GitBranchDto, GitCommitDto } from '../../../../api';
import * as cytoscape from 'cytoscape';
import { EdgeDefinition, NodeDefinition } from 'cytoscape';
import * as dagre from 'cytoscape-dagre';
import { DagreLayoutOptions } from 'cytoscape-dagre';
import { GitService } from "../../../services/git.service";

@Component({
  selector: 'app-active-conflict-awareness',
  templateUrl: './active-conflict-awareness.component.html',
  styleUrls: ['./active-conflict-awareness.component.scss']
})
export class ActiveConflictAwarenessComponent implements OnInit {

  private gitService = inject(GitService)

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
      this.selectedCommit = commit;
      this.gitService.getBranchesByCommitId(commit.id).then(branches => {
        return this.selectedCommitsBranches = branches;
      })
      this.gitService.loadChangesOfCommit(commit);
    })
  }



}
