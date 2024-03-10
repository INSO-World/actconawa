import { Component, inject, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { GitService } from "../../../../services/git.service";
import {
  GitCommitDiffFileDto,
  GitCommitDiffHunkDto,
  GitCommitDiffLineChangeDto,
  GitCommitDto
} from "../../../../../api";

@Component({
  selector: 'app-active-conflict-awareness-diff',
  standalone: false,
  templateUrl: './active-conflict-awareness-diff.component.html',
  styleUrls: ['./active-conflict-awareness-diff.component.scss']
})
export class ActiveConflictAwarenessDiffComponent implements OnInit, OnChanges {

  protected gitService = inject(GitService)

  @Input()
  commit!: GitCommitDto;

  private currentCommit?: GitCommitDto

  hunksByFileId = new Map<string, GitCommitDiffHunkDto[]>();

  changedLinesByFileId = new Map<string, GitCommitDiffLineChangeDto[]>();

  changedCodeByFileId = new Map<string, GitCommitDiffLineChangeDto[]>();

  diffFilesByParentCommitId = new Map<string, GitCommitDiffFileDto[]>

  diffFilesById = new Map<string, GitCommitDiffFileDto>

  ngOnInit(): void {
    this.refresh();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.refresh();
  }

  refresh() {
    this.clear();
    if (this.currentCommit?.id === this.commit.id) {
      return;
    }
    this.currentCommit = this.commit;
    for (const parentId of this.commit?.parentIds || []) {
      this.gitService.getModifiedFilesByCommitIds(this.commit.id || "", parentId).then(diffs => {
        this.diffFilesByParentCommitId.set(parentId, diffs || []);
        for (const diff of diffs || []) {
          this.diffFilesById.set(diff.id || "", diff)
          if (diff && diff.id) {
            this.gitService.getHunksByDiffFileId(diff.id).then(changes => {
              this.hunksByFileId.set(diff.id || "", changes || []);
            });
            this.gitService.getLineChangesByDiffFileId(diff.id).then(changes => {
              this.changedLinesByFileId.set(diff.id || "", changes || []);
            });
            this.gitService.getCodeChangesByDiffFileId(diff.id).then(changes => {
              this.changedCodeByFileId.set(diff.id || "", changes || []);
            });
          }
        }
      })
    }
  }

  clear() {
    this.hunksByFileId = new Map<string, GitCommitDiffHunkDto[]>();
    this.changedLinesByFileId = new Map<string, GitCommitDiffLineChangeDto[]>();
    this.changedCodeByFileId = new Map<string, GitCommitDiffLineChangeDto[]>();
    this.diffFilesByParentCommitId = new Map<string, GitCommitDiffFileDto[]>
    this.diffFilesById = new Map<string, GitCommitDiffFileDto>
  }
}
