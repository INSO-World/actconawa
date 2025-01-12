import { Component, inject } from '@angular/core';
import { GitService } from "../../../services/git.service";
import { GitBranchDto } from "../../../../api";
import { SettingService } from "../../../services/setting.service";

@Component({
  selector: 'app-repository-configuration',
  standalone: false,
  templateUrl: './repository-configuration.component.html',
  styleUrls: ['./repository-configuration.component.scss']
})
export class RepositoryConfigurationComponent {

  protected gitService = inject(GitService)

  protected settingService = inject(SettingService)

  protected branches: GitBranchDto[] = []

  async ngOnInit() {
    this.branches = await this.gitService.getBranches();
  }

  onSelectReferenceBranch(event: Event) {
    this.settingService.setReferenceBranchName((event.target as HTMLSelectElement).value)
  }
}
