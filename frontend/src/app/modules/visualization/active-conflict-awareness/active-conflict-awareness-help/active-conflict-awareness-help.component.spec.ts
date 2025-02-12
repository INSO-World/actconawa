import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActiveConflictAwarenessHelpComponent } from './active-conflict-awareness-help.component';

describe('ActiveConflictAwarenessHelpComponent', () => {
  let component: ActiveConflictAwarenessHelpComponent;
  let fixture: ComponentFixture<ActiveConflictAwarenessHelpComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActiveConflictAwarenessHelpComponent]
    })
            .compileComponents();

    fixture = TestBed.createComponent(ActiveConflictAwarenessHelpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
