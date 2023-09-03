import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeviceSummaryReportComponent } from './device-summary-report.component';

describe('DeviceSummaryReportComponent', () => {
  let component: DeviceSummaryReportComponent;
  let fixture: ComponentFixture<DeviceSummaryReportComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeviceSummaryReportComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeviceSummaryReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
