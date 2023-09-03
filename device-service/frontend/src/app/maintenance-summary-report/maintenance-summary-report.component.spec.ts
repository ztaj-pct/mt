import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MaintenanceSummaryReportComponent } from './maintenance-summary-report.component';

describe('MaintenanceSummaryReportComponent', () => {
  let component: MaintenanceSummaryReportComponent;
  let fixture: ComponentFixture<MaintenanceSummaryReportComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MaintenanceSummaryReportComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MaintenanceSummaryReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
