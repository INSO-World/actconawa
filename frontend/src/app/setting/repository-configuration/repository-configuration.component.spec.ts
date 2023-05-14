import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RepositoryConfigurationComponent } from './repository-configuration.component';

describe('RepositoryConfigurationComponent', () => {
  let component: RepositoryConfigurationComponent;
  let fixture: ComponentFixture<RepositoryConfigurationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RepositoryConfigurationComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RepositoryConfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
