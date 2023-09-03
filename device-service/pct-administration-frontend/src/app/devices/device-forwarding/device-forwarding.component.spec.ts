import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeviceForwardingComponent } from './device-forwarding.component';

describe('DeviceForwardingComponent', () => {
  let component: DeviceForwardingComponent;
  let fixture: ComponentFixture<DeviceForwardingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DeviceForwardingComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DeviceForwardingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
