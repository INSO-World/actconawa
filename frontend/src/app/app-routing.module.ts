import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'visualization',
    loadChildren: () => import('./modules/visualization/visualization.module').then(m => m.VisualizationModule)
  },
  {
    path: 'setting',
    loadChildren: () => import('./modules/setting/setting.module').then(m => m.SettingModule)
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule { }
