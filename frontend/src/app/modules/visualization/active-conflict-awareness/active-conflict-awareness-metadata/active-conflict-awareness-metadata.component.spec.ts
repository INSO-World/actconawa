import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActiveConflictAwarenessMetadataComponent } from './active-conflict-awareness-metadata.component';

describe('ActiveConflictAwarenessMetadataComponent', () => {
  let component: ActiveConflictAwarenessMetadataComponent;
  let fixture: ComponentFixture<ActiveConflictAwarenessMetadataComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ActiveConflictAwarenessMetadataComponent]
    });
    fixture = TestBed.createComponent(ActiveConflictAwarenessMetadataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
