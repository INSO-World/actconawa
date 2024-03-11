import { Component, Input } from '@angular/core';
import { GitBranchDto, GitCommitDto } from "../../../../../api";
import { MatSnackBar } from "@angular/material/snack-bar";

@Component({
  selector: 'app-active-conflict-awareness-commit',
  standalone: false,
  templateUrl: './active-conflict-awareness-commit.component.html',
  styleUrls: ['./active-conflict-awareness-commit.component.scss']
})
export class ActiveConflictAwarenessCommitComponent {

  @Input()
  commit?: GitCommitDto;

  @Input()
  branches: GitBranchDto[] | undefined;

  constructor(private _snackBar: MatSnackBar) {
  }

  copyText(text: string | undefined) {
    navigator.clipboard.writeText(text || "").then();
    this._snackBar.open("Copied to clipboard", "Ok", {duration: 3000});
  }
}
