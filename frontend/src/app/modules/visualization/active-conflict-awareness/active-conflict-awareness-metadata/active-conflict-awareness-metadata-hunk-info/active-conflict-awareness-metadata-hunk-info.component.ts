import { Component, Input } from '@angular/core';
import { GitCommitDiffHunkDto } from "../../../../../../api";

@Component({
  selector: 'app-active-conflict-awareness-metadata-hunk-info',
  standalone: true,
  imports: [],
  templateUrl: './active-conflict-awareness-metadata-hunk-info.component.html',
  styleUrl: './active-conflict-awareness-metadata-hunk-info.component.scss'
})
export class ActiveConflictAwarenessMetadataHunkInfoComponent {

  @Input()
  hunk?: GitCommitDiffHunkDto;

}
