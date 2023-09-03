import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BatchDeviceEditComponent } from './batch-device-edit.component';

describe('BatchDeviceEditComponent', () => {
  let component: BatchDeviceEditComponent;
  let fixture: ComponentFixture<BatchDeviceEditComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BatchDeviceEditComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BatchDeviceEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
