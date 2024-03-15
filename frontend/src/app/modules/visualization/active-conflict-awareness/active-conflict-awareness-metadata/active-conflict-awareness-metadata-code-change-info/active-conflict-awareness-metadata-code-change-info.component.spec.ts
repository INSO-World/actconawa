import { ComponentFixture, TestBed } from '@angular/core/testing';

import {
  ActiveConflictAwarenessMetadataCodeChangeInfoComponent
} from './active-conflict-awareness-metadata-code-change-info.component';

describe('ActiveConflictAwarenessMetadataCodeChangeInfoComponent', () => {
  let component: ActiveConflictAwarenessMetadataCodeChangeInfoComponent;
  let fixture: ComponentFixture<ActiveConflictAwarenessMetadataCodeChangeInfoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActiveConflictAwarenessMetadataCodeChangeInfoComponent]
    })
            .compileComponents();

    fixture = TestBed.createComponent(ActiveConflictAwarenessMetadataCodeChangeInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
