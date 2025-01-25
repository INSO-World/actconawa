import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActiveConflictAwarenessConflictsComponent } from './active-conflict-awareness-conflicts.component';

describe('ActiveConflictAwarenessConflictsComponent', () => {
  let component: ActiveConflictAwarenessConflictsComponent;
  let fixture: ComponentFixture<ActiveConflictAwarenessConflictsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActiveConflictAwarenessConflictsComponent]
    })
            .compileComponents();

    fixture = TestBed.createComponent(ActiveConflictAwarenessConflictsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
