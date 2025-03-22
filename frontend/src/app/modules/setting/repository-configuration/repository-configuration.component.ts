import { Component, inject, OnInit } from '@angular/core';
import { GitService } from "../../../services/git.service";
import { GitBranchDto } from "../../../../api";
import { SettingService } from "../../../services/setting.service";
import { MatCheckboxChange } from "@angular/material/checkbox";

@Component({
  selector: 'app-repository-configuration',
  standalone: false,
  templateUrl: './repository-configuration.component.html',
  styleUrls: ['./repository-configuration.component.scss']
})
export class RepositoryConfigurationComponent implements OnInit {

  protected gitService = inject(GitService)

  protected settingService = inject(SettingService)

  protected branches: GitBranchDto[] = []

  async ngOnInit() {
    this.branches = await this.gitService.getBranches();
  }

  onSelectReferenceBranch(event: Event) {
    this.settingService.setReferenceBranchName((event.target as HTMLSelectElement).value)
  }

  onBranchLabelColoringSettingChanged(change: MatCheckboxChange) {
    this.settingService.setBranchLabelColoringEnabled(change.checked)
  }
}
