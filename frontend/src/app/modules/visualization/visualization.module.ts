import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { VisualizationRoutingModule } from './visualization-routing.module';
import { ActiveConflictAwarenessComponent } from "./active-conflict-awareness/active-conflict-awareness.component";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatCardModule } from "@angular/material/card";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatExpansionModule } from "@angular/material/expansion";
import {
  ActiveConflictAwarenessCommitComponent
} from './active-conflict-awareness/active-conflict-awareness-commit/active-conflict-awareness-commit.component';
import { MatGridListModule } from "@angular/material/grid-list";
import { MatChipsModule } from "@angular/material/chips";
import {
  ActiveConflictAwarenessDiffComponent
} from './active-conflict-awareness/active-conflict-awareness-diff/active-conflict-awareness-diff.component';
import { MatTabsModule } from "@angular/material/tabs";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatListModule } from "@angular/material/list";
import { DirectivesModule } from "../../directives/directives.module";
import { MatFormField, MatInput } from "@angular/material/input";
import {
  ActiveConflictAwarenessMetadataComponent
} from "./active-conflict-awareness/active-conflict-awareness-metadata/active-conflict-awareness-metadata.component";
import { MatDialogTitle } from "@angular/material/dialog";
import {
  ActiveConflictAwarenessMetadataHunkInfoComponent
} from "./active-conflict-awareness/active-conflict-awareness-metadata/active-conflict-awareness-metadata-hunk-info/active-conflict-awareness-metadata-hunk-info.component";
import {
  ActiveConflictAwarenessMetadataLineChangeInfoComponent
} from "./active-conflict-awareness/active-conflict-awareness-metadata/active-conflict-awareness-metadata-line-change-info/active-conflict-awareness-metadata-line-change-info.component";
import {
  ActiveConflictAwarenessMetadataCodeChangeInfoComponent
} from "./active-conflict-awareness/active-conflict-awareness-metadata/active-conflict-awareness-metadata-code-change-info/active-conflict-awareness-metadata-code-change-info.component";

@NgModule({
  declarations: [
    ActiveConflictAwarenessComponent,
    ActiveConflictAwarenessCommitComponent,
    ActiveConflictAwarenessDiffComponent,
    ActiveConflictAwarenessMetadataComponent
  ],
  imports: [
    CommonModule,
    DirectivesModule,
    VisualizationRoutingModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatTooltipModule,
    MatExpansionModule,
    MatGridListModule,
    MatChipsModule,
    MatTabsModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    DirectivesModule,
    MatInput,
    MatFormField,
    MatDialogTitle,
    ActiveConflictAwarenessMetadataHunkInfoComponent,
    ActiveConflictAwarenessMetadataLineChangeInfoComponent,
    ActiveConflictAwarenessMetadataCodeChangeInfoComponent,
  ]
})
export class VisualizationModule {
}
