import { ComponentFixture, TestBed } from '@angular/core/testing';

import {
  ActiveConflictAwarenessCommitComponent
} from './active-conflict-awareness-commit.component';

describe('ActiveConflictAwarenessCommitComponent', () => {
  let component: ActiveConflictAwarenessCommitComponent;
  let fixture: ComponentFixture<ActiveConflictAwarenessCommitComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ActiveConflictAwarenessCommitComponent]
    })
            .compileComponents();

    fixture = TestBed.createComponent(ActiveConflictAwarenessCommitComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
