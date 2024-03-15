import { Component, Input } from '@angular/core';
import { GitCommitDiffHunkDto } from "../../../../../../api";
import { NgForOf, NgIf } from "@angular/common";
import { MatDivider } from "@angular/material/divider";

@Component({
  selector: 'app-active-conflict-awareness-metadata-hunk-info',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    MatDivider],
  templateUrl: './active-conflict-awareness-metadata-hunk-info.component.html',
  styleUrl: './active-conflict-awareness-metadata-hunk-info.component.scss'
})
export class ActiveConflictAwarenessMetadataHunkInfoComponent {

  @Input()
  hunk?: GitCommitDiffHunkDto;

}
