import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SettingRoutingModule } from './setting-routing.module';
import { RepositoryConfigurationComponent } from './repository-configuration/repository-configuration.component';
import { MatFormField, MatLabel } from "@angular/material/form-field";
import { MatCard } from "@angular/material/card";
import { MatInput } from "@angular/material/input";
import { MatCheckbox } from "@angular/material/checkbox";

@NgModule({
  declarations: [
    RepositoryConfigurationComponent
  ],
  imports: [
    CommonModule,
    SettingRoutingModule,
    MatLabel,
    MatFormField,
    MatCard,
    MatInput,
    MatCheckbox
  ]
})
export class SettingModule { }
