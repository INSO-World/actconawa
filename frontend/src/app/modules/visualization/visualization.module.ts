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

@NgModule({
  declarations: [
    ActiveConflictAwarenessComponent,
    ActiveConflictAwarenessCommitComponent,
    ActiveConflictAwarenessDiffComponent
  ],
  imports: [
    CommonModule,
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
  ]
})
export class VisualizationModule {
}
