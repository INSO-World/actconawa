import { Component, inject, Input, OnChanges, OnInit } from '@angular/core';
import { GitService } from "../../../../services/git.service";
import {
  GitCommitDiffCodeChangeDto,
  GitCommitDiffFileDto,
  GitCommitDiffHunkDto,
  GitCommitDiffLineChangeDto,
  GitCommitDto,
  GitPatchDto
} from "../../../../../api";

@Component({
  selector: 'app-active-conflict-awareness-metadata',
  standalone: false,
  templateUrl: './active-conflict-awareness-metadata.component.html',
  styleUrls: ['./active-conflict-awareness-metadata.component.scss']
})
export class ActiveConflictAwarenessMetadataComponent implements OnInit, OnChanges {

  protected gitService = inject(GitService)

  @Input()
  commit!: GitCommitDto;

  @Input()
  selectedParentCommitId!: string;

  hunksByFileId = new Map<string, GitCommitDiffHunkDto[]>();

  changedLinesByFileId = new Map<string, GitCommitDiffLineChangeDto[]>();

  changedCodeByFileId = new Map<string, GitCommitDiffCodeChangeDto[]>();

  diffFilesByParentCommitId = new Map<string, GitCommitDiffFileDto[]>

  diffFilesById = new Map<string, GitCommitDiffFileDto>

  patchByParentCommitId = new Map<string, GitPatchDto>;

  selectedFile?: GitCommitDiffFileDto;

  private currentCommit?: GitCommitDto

  private readonly pathContextLines = 3;

  ngOnInit(): void {
    this.refresh();
  }

  ngOnChanges(): void {
    this.refresh();
  }

  refresh() {
    this.clear();
    if (this.currentCommit?.id === this.commit.id) {
      return;
    }
    this.currentCommit = this.commit;

    this.gitService.getModifiedFilesByCommitIds(this.commit.id || "", this.selectedParentCommitId).then(diffs => {
      this.diffFilesByParentCommitId.set(this.selectedParentCommitId, diffs || []);
      this.gitService.getPatch(this.commit.id || "", this.selectedParentCommitId, this.pathContextLines).then(patch => {
        this.patchByParentCommitId.set(this.selectedParentCommitId, patch);
      })
      for (const diff of diffs || []) {
        if (!this.selectedFile) {
          this.selectedFile = diff;
        }
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

  clear() {
    this.hunksByFileId = new Map<string, GitCommitDiffHunkDto[]>();
    this.changedLinesByFileId = new Map<string, GitCommitDiffLineChangeDto[]>();
    this.changedCodeByFileId = new Map<string, GitCommitDiffLineChangeDto[]>();
    this.diffFilesByParentCommitId = new Map<string, GitCommitDiffFileDto[]>();
    this.diffFilesById = new Map<string, GitCommitDiffFileDto>();
    this.patchByParentCommitId = new Map<string, GitPatchDto>();
  }
}
