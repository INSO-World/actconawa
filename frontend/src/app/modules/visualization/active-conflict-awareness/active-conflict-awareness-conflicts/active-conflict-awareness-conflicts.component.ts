import { Component, inject, Input } from '@angular/core';
import { MatTab, MatTabGroup } from "@angular/material/tabs";
import { AsyncPipe, JsonPipe, NgForOf, NgIf, SlicePipe } from "@angular/common";
import { GitService } from "../../../../services/git.service";
import { GitBranchDto, GitBranchTrackingStatusDto } from "../../../../../api";
import { DirectivesModule } from "../../../../directives/directives.module";
import { MatList, MatListItem } from "@angular/material/list";
import { MatDivider } from "@angular/material/divider";
import { MatIcon } from "@angular/material/icon";

@Component({
  selector: 'app-active-conflict-awareness-conflicts',
  standalone: true,
  imports: [
    MatTab,
    MatTabGroup,
    NgForOf,
    NgIf,
    SlicePipe,
    JsonPipe,
    AsyncPipe,
    DirectivesModule,
    MatList,
    MatListItem,
    MatDivider,
    MatIcon
  ],
  templateUrl: './active-conflict-awareness-conflicts.component.html',
  styleUrl: './active-conflict-awareness-conflicts.component.scss'
})
export class ActiveConflictAwarenessConflictsComponent {
  @Input()
  selectedBranches: GitBranchDto[] = [];

  branchIdToBranchMap = new Map<string, GitBranchDto>();

  protected gitService = inject(GitService)

  protected trackingStatus: GitBranchTrackingStatusDto[] = []

  constructor() {
  }

  async ngOnInit(): Promise<void> {

    const branches = (await this.gitService.getBranches())
            .map(b => this.branchIdToBranchMap.set(b.id || "", b));

    await this.refresh()
  }

  async ngOnChanges(): Promise<void> {
    await this.refresh();
  }

  async refresh() {
    this.clear()
    if (this.selectedBranches.length > 0) {
      this.trackingStatus = await this.gitService.getBranchTrackingStatusById(this.selectedBranches[ 0 ].id || "");
    }
  }

  async getBranchNameById(branchId?: string) {
    if (!branchId) {
      throw new Error('Required parameter branchId was null or undefined when calling getBranchNameById.');
    }
  }

  clear() {
    this.trackingStatus = [];
  }

  getProblemBranchCount() {
    return this.trackingStatus
            .filter(ts => ts.mergeStatus === 'CONFLICTS' || ts.mergeStatus === 'UNKNOWN_MERGE_BASE')
            .length;
  }

}
