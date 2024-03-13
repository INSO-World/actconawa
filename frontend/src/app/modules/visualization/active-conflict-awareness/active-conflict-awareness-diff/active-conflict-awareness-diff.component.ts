import { Component, Inject, inject, Input, OnInit } from '@angular/core';
import { GitService } from "../../../../services/git.service";
import { GitCommitDto, GitPatchDto } from "../../../../../api";
import { MAT_DIALOG_DATA } from "@angular/material/dialog";

@Component({
  selector: 'app-active-conflict-awareness-diff',
  standalone: false,
  templateUrl: './active-conflict-awareness-diff.component.html',
  styleUrls: ['./active-conflict-awareness-diff.component.scss']
})
export class ActiveConflictAwarenessDiffComponent implements OnInit {

  protected gitService = inject(GitService)

  @Input()
  commit!: GitCommitDto;

  private readonly pathContextLines = 3;

  patchByParentCommitId = new Map<string, GitPatchDto>;

  constructor(@Inject(MAT_DIALOG_DATA) public data: GitCommitDto) {
    this.commit = data;
  }

  ngOnInit(): void {
    this.refresh();
  }

  refresh() {
    this.clear();
    for (const parentId of this.commit?.parentIds || []) {
      this.gitService.getModifiedFilesByCommitIds(this.commit.id || "", parentId).then(diffs => {
        this.gitService.getPatch(this.commit.id || "", parentId, this.pathContextLines).then(patch => {
          this.patchByParentCommitId.set(parentId, patch);
        })
      })
    }
  }

  clear() {
    this.patchByParentCommitId = new Map<string, GitPatchDto>();
  }
}
