import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActiveConflictAwarenessComponent } from './active-conflict-awareness.component';

describe('ActiveConflictAwarenessComponent', () => {
  let component: ActiveConflictAwarenessComponent;
  let fixture: ComponentFixture<ActiveConflictAwarenessComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ActiveConflictAwarenessComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActiveConflictAwarenessComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
