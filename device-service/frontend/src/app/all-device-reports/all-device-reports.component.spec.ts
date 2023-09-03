import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AllDeviceReportsComponent } from './all-device-reports.component';

describe('AllDeviceReportsComponent', () => {
  let component: AllDeviceReportsComponent;
  let fixture: ComponentFixture<AllDeviceReportsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AllDeviceReportsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AllDeviceReportsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
