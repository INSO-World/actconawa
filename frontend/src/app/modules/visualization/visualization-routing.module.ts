import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ActiveConflictAwarenessComponent } from "./active-conflict-awareness/active-conflict-awareness.component";

const routes: Routes = [
  {
    path: 'active-conflict-awareness',
    component: ActiveConflictAwarenessComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class VisualizationRoutingModule { }
