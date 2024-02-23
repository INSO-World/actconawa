import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RepositoryConfigurationComponent } from "./repository-configuration/repository-configuration.component";

const routes: Routes = [
  {
    path: "repository",
    component: RepositoryConfigurationComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SettingRoutingModule { }
