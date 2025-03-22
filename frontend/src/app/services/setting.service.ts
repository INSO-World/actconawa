import { Injectable } from '@angular/core';
import { GitService } from "./git.service";
import { MatSnackBar } from "@angular/material/snack-bar";

@Injectable({
  providedIn: 'root'
})
export class SettingService {

  private readonly DEFAULT_REFERENCE_BRANCH = "origin/main"

  constructor(private gitService: GitService, private _snackBar: MatSnackBar) {
  }

  getReferenceBranchName() {
    const ref = localStorage.getItem('referenceBranch');
    if (!ref) {
      localStorage.setItem('referenceBranch', this.DEFAULT_REFERENCE_BRANCH);
      return this.DEFAULT_REFERENCE_BRANCH;
    }
    return ref;
  }

  setReferenceBranchName(referenceBranchName: string) {
    localStorage.setItem('referenceBranch', referenceBranchName)
  }

  getBranchLabelColoringEnabled() {
    const ref = localStorage.getItem('branchLabelColoringEnabled') === '1';
    if (!ref) {
      localStorage.setItem('branchLabelColoringEnabled', '0');
      return false;
    }
    return ref;
  }

  setBranchLabelColoringEnabled(enabled: boolean) {
    localStorage.setItem('branchLabelColoringEnabled', enabled ? '1' : '0')
  }

  async getReferenceBranchId(): Promise<string> {
    const result =
            (await this.gitService.getBranches()).find(x => x.name === this.getReferenceBranchName());
    if (!result || !result.id) {
      this._snackBar.open(
              "Couldn't find reference branch. Check settings",
              "Ok",
              {duration: 3000, panelClass: "error-snackbar"});
      return "";
    } else {
      return result.id;
    }
  }
}
