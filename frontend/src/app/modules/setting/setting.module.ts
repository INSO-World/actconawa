import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SettingRoutingModule } from './setting-routing.module';
import { RepositoryConfigurationComponent } from './repository-configuration/repository-configuration.component';

@NgModule({
  declarations: [
    RepositoryConfigurationComponent
  ],
  imports: [
    CommonModule,
    SettingRoutingModule
  ]
})
export class SettingModule { }
