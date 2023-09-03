import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeviceReportCountComponentComponent } from './device-report-count-component.component';

describe('DeviceReportCountComponentComponent', () => {
  let component: DeviceReportCountComponentComponent;
  let fixture: ComponentFixture<DeviceReportCountComponentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeviceReportCountComponentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeviceReportCountComponentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
