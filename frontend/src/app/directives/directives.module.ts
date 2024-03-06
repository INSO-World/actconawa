import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ResizeDirective } from './resize.directive';

@NgModule({
  declarations: [
    ResizeDirective
  ],
  imports: [
    CommonModule
  ],
  exports: [
    ResizeDirective,
  ]
})
export class DirectivesModule {
}
