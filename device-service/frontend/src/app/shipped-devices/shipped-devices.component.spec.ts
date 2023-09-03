import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ShippedDevicesComponent } from './shipped-devices.component';

describe('ShippedDevicesComponent', () => {
  let component: ShippedDevicesComponent;
  let fixture: ComponentFixture<ShippedDevicesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ShippedDevicesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ShippedDevicesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
