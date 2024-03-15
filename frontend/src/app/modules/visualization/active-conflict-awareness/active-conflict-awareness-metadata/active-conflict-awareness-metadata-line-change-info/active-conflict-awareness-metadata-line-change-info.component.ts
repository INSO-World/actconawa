import { Component, Input } from '@angular/core';
import { GitCommitDiffLineChangeDto } from "../../../../../../api";

@Component({
  selector: 'app-active-conflict-awareness-metadata-line-change-info',
  standalone: true,
  imports: [],
  templateUrl: './active-conflict-awareness-metadata-line-change-info.component.html',
  styleUrl: './active-conflict-awareness-metadata-line-change-info.component.scss'
})
export class ActiveConflictAwarenessMetadataLineChangeInfoComponent {

  @Input()
  lines?: GitCommitDiffLineChangeDto;

}
