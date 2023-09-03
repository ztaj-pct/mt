import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UploadGatewayComponent } from './upload-gateway.component';

describe('GatewayComponent', () => {
  let component: UploadGatewayComponent;
  let fixture: ComponentFixture<UploadGatewayComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UploadGatewayComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UploadGatewayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
