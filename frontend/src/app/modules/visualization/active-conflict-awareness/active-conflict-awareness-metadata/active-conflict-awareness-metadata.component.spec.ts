import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActiveConflictAwarenessDiffComponent } from './active-conflict-awareness-metadata.component';

describe('ActiveConflictAwarenessDiffComponent', () => {
  let component: ActiveConflictAwarenessDiffComponent;
  let fixture: ComponentFixture<ActiveConflictAwarenessDiffComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ActiveConflictAwarenessDiffComponent]
    });
    fixture = TestBed.createComponent(ActiveConflictAwarenessDiffComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
