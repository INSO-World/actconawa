import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { VisualizationRoutingModule } from './visualization-routing.module';
import {
  ActiveConflictAwarenessComponent
} from "./active-conflict-awareness/active-conflict-awareness.component";

@NgModule({
  declarations: [
    ActiveConflictAwarenessComponent
  ],
  imports: [
    CommonModule,
    VisualizationRoutingModule
  ]
})
export class VisualizationModule {
}
