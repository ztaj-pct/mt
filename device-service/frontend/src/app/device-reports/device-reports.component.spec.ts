import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeviceReportsComponent } from './device-reports.component';

describe('DeviceReportsComponent', () => {
  let component: DeviceReportsComponent;
  let fixture: ComponentFixture<DeviceReportsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeviceReportsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeviceReportsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
