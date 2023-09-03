import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UploadAssetAssociationsComponent } from './upload-asset-associations.component';

describe('UploadAssetAssociationsComponent', () => {
  let component: UploadAssetAssociationsComponent;
  let fixture: ComponentFixture<UploadAssetAssociationsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UploadAssetAssociationsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UploadAssetAssociationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
