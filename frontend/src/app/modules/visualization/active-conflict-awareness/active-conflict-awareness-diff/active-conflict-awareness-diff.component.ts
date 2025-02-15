import { AfterViewInit, Component, ElementRef, Inject, inject, Input } from '@angular/core';
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
export class ActiveConflictAwarenessDiffComponent implements AfterViewInit {

  protected gitService = inject(GitService)

  @Input()
  commit!: GitCommitDto;

  private readonly pathContextLines = 3;

  shaByParentCommitId = new Map<string, string>;

  patchByParentCommitId = new Map<string, GitPatchDto>;

  constructor(@Inject(MAT_DIALOG_DATA) public data: GitCommitDto,
          private el: ElementRef) {
    this.commit = data;
  }

  async ngAfterViewInit() {
    this.refresh();
  }

  async refresh() {
    this.clear();
    for (const parentId of this.commit?.parentIds || []) {
      const parentCommit = await this.gitService.getCommitById(parentId);
      this.shaByParentCommitId.set(parentId, parentCommit?.sha || "");
      this.gitService.getModifiedFilesByCommitIds(this.commit.id || "", parentId).then(() => {
        this.gitService.getPatch(this.commit.id || "", parentId, this.pathContextLines).then(patch => {
          this.patchByParentCommitId.set(parentId, patch);
          const patchViewer = this.el.nativeElement.querySelector('#patch-viewer-' + parentId)
          const diff2htmlUi = new Diff2HtmlUI(patchViewer, patch.patch, {
            drawFileList: false,
            matching: 'lines',
            stickyFileHeaders: false,
            synchronisedScroll: false
          });
          diff2htmlUi.draw();
        })
      })
    }
  }
  clear() {
    this.patchByParentCommitId = new Map<string, GitPatchDto>();
  }
}
