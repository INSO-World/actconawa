import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { VisualizationRoutingModule } from './visualization-routing.module';
import {
  ActiveConflictAwarenessComponent
} from "./active-conflict-awareness/active-conflict-awareness.component";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatCardModule } from "@angular/material/card";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatExpansionModule } from "@angular/material/expansion";

@NgModule({
  declarations: [
    ActiveConflictAwarenessComponent
  ],
  imports: [
    CommonModule,
    VisualizationRoutingModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatTooltipModule,
    MatExpansionModule,
  ]
})
export class VisualizationModule {
}
