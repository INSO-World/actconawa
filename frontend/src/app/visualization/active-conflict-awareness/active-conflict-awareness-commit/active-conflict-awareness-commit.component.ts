import { Component, Input } from '@angular/core';
import { GitBranchDto, GitCommitDto } from "../../../../api";

@Component({
  selector: 'app-active-conflict-awareness-commit',
  templateUrl: './active-conflict-awareness-commit.component.html',
  styleUrls: ['./active-conflict-awareness-commit.component.scss']
})
export class ActiveConflictAwarenessCommitComponent {

  @Input()
  commit?: GitCommitDto;

  @Input()
  branches: GitBranchDto[] | undefined;

}
