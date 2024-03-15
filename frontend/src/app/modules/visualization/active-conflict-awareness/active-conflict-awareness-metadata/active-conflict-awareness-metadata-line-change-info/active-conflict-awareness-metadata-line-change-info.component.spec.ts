import { ComponentFixture, TestBed } from '@angular/core/testing';

import {
  ActiveConflictAwarenessMetadataLineChangeInfoComponent
} from './active-conflict-awareness-metadata-line-change-info.component';

describe('ActiveConflictAwarenessMetadataLineChangeInfoComponent', () => {
  let component: ActiveConflictAwarenessMetadataLineChangeInfoComponent;
  let fixture: ComponentFixture<ActiveConflictAwarenessMetadataLineChangeInfoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActiveConflictAwarenessMetadataLineChangeInfoComponent]
    })
            .compileComponents();

    fixture = TestBed.createComponent(ActiveConflictAwarenessMetadataLineChangeInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
