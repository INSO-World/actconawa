import { AfterViewInit, Component, ElementRef, Inject, inject, Input, OnInit } from '@angular/core';
import { GitService } from "../../../../services/git.service";
import { GitCommitDto, GitPatchDto } from "../../../../../api";
import { MAT_DIALOG_DATA } from "@angular/material/dialog";
import { Diff2HtmlUI } from "diff2html/lib-esm/ui/js/diff2html-ui";

@Component({
  selector: 'app-active-conflict-awareness-diff',
  standalone: false,
  templateUrl: './active-conflict-awareness-diff.component.html',
  styleUrls: ['./active-conflict-awareness-diff.component.scss']
})
export class ActiveConflictAwarenessDiffComponent implements OnInit, AfterViewInit {

  protected gitService = inject(GitService)

  @Input()
  commit!: GitCommitDto;

  private readonly pathContextLines = 3;

  patchByParentCommitId = new Map<string, GitPatchDto>;

  constructor(@Inject(MAT_DIALOG_DATA) public data: GitCommitDto,
          private el: ElementRef) {
    this.commit = data;
  }

  ngOnInit(): void {
  }

  ngAfterViewInit() {
    this.refresh();
  }

  refresh() {
    this.clear();
    for (const parentId of this.commit?.parentIds || []) {
      this.gitService.getModifiedFilesByCommitIds(this.commit.id || "", parentId).then(diffs => {
        this.gitService.getPatch(this.commit.id || "", parentId, this.pathContextLines).then(patch => {
          this.patchByParentCommitId.set(parentId, patch);
          const patchViewer = this.el.nativeElement.querySelector('#patch-viewer-' + parentId)
          const configuration = {drawFileList: true, matching: 'lines'};
          const diff2htmlUi = new Diff2HtmlUI(patchViewer, patch.patch);
          diff2htmlUi.draw();
        })
      })
    }
  }

  clear() {
    this.patchByParentCommitId = new Map<string, GitPatchDto>();
  }
}
