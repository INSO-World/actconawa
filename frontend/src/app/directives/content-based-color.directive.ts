import { AfterViewInit, Directive, ElementRef, inject } from '@angular/core';
import { SettingService } from "../services/setting.service";

@Directive({
  selector: '[appContentBasedColor]',
  standalone: false
})
export class ContentBasedColorDirective implements AfterViewInit {

  protected settingService = inject(SettingService)

  constructor(private el: ElementRef) {
  }

  ngAfterViewInit(): void {
    if (this.settingService.getBranchLabelColoringEnabled()) {
      this.colorize(this.el.nativeElement.textContent)
    }
  }

  colorize(text: string) {
    let hash = 0;
    for (let i = 0; i < text.length; i++) {
      hash = text.charCodeAt(i) + ((hash << 5) - hash);
    }
    this.el.nativeElement.style.backgroundColor = `hsl(
      ${Math.abs(hash) % 360},
      ${Math.abs(hash) % 30 + 70}%,
      ${Math.abs(hash) % 10 + 80}%
    )`;
  }

}
