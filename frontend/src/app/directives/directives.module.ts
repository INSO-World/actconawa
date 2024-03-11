import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ResizeDirective } from './resize.directive';
import { ContentBasedColorDirective } from "./content-based-color.directive";

@NgModule({
  declarations: [
    ContentBasedColorDirective,
    ResizeDirective
  ],
  imports: [
    CommonModule
  ],
  exports: [
    ContentBasedColorDirective,
    ResizeDirective,
  ]
})
export class DirectivesModule {
}
