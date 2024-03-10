import { Directive, ElementRef, EventEmitter, Input, NgZone, OnDestroy, OnInit, Output } from '@angular/core';
import { debounceTime } from "rxjs";

@Directive({
  selector: '[appResize]',
  standalone: false
})
export class ResizeDirective implements OnInit, OnDestroy {

  @Output()
  readonly appResize = new EventEmitter<void>();

  @Input()
  readonly debounceTime = 300;

  private readonly debouncer = new EventEmitter<void>();

  private observer: ResizeObserver;

  constructor(
          private readonly el: ElementRef,
          private readonly zone: NgZone
  ) {
    this.observer = new ResizeObserver(() => this.zone.run(() => {
      this.debouncer.emit();
    }));
  }

  public ngOnInit(): void {
    this.observer.observe(this.el.nativeElement)
    this.debouncer.pipe(debounceTime(this.debounceTime))
            .subscribe(() => this.appResize.emit());
  }

  public ngOnDestroy(): void {
    this.observer.disconnect();
    this.debouncer.unsubscribe();
  }

}
