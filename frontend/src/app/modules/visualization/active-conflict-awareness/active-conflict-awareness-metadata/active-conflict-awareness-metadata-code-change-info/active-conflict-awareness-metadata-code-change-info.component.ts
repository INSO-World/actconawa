import { Component, Input } from '@angular/core';
import { NgForOf, NgIf } from "@angular/common";
import { GitCommitDiffCodeChangeDto } from "../../../../../../api";
import { MatDivider } from "@angular/material/divider";

@Component({
  selector: 'app-active-conflict-awareness-metadata-code-change-info',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    MatDivider
  ],
  templateUrl: './active-conflict-awareness-metadata-code-change-info.component.html',
  styleUrl: './active-conflict-awareness-metadata-code-change-info.component.scss'
})
export class ActiveConflictAwarenessMetadataCodeChangeInfoComponent {

  @Input()
  codechanges?: GitCommitDiffCodeChangeDto;

}
