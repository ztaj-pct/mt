import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ResetInstallationStatusComponent } from './reset-installation-status.component';

describe('ResetInstallationStatusComponent', () => {
  let component: ResetInstallationStatusComponent;
  let fixture: ComponentFixture<ResetInstallationStatusComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ResetInstallationStatusComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResetInstallationStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
