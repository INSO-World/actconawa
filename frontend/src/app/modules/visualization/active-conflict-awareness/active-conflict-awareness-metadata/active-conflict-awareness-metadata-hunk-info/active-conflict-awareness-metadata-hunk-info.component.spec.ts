import { ComponentFixture, TestBed } from '@angular/core/testing';

import {
  ActiveConflictAwarenessMetadataHunkInfoComponent
} from './active-conflict-awareness-metadata-hunk-info.component';

describe('ActiveConflictAwarenessMetadataHunkInfoComponent', () => {
  let component: ActiveConflictAwarenessMetadataHunkInfoComponent;
  let fixture: ComponentFixture<ActiveConflictAwarenessMetadataHunkInfoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActiveConflictAwarenessMetadataHunkInfoComponent]
    })
            .compileComponents();

    fixture = TestBed.createComponent(ActiveConflictAwarenessMetadataHunkInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
