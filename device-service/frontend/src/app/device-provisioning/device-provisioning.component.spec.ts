import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeviceProvisioningComponent } from './device-provisioning.component';

describe('DeviceProvisioningComponent', () => {
  let component: DeviceProvisioningComponent;
  let fixture: ComponentFixture<DeviceProvisioningComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeviceProvisioningComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeviceProvisioningComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
