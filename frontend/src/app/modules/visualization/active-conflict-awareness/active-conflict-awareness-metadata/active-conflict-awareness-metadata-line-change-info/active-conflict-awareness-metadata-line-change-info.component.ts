import { Component, Input } from '@angular/core';
import { GitCommitDiffLineChangeDto } from "../../../../../../api";
import { NgForOf, NgIf } from "@angular/common";
import { MatDivider } from "@angular/material/divider";

@Component({
  selector: 'app-active-conflict-awareness-metadata-line-change-info',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    MatDivider],
  templateUrl: './active-conflict-awareness-metadata-line-change-info.component.html',
  styleUrl: './active-conflict-awareness-metadata-line-change-info.component.scss'
})
export class ActiveConflictAwarenessMetadataLineChangeInfoComponent {

  @Input()
  lines?: GitCommitDiffLineChangeDto;

}
