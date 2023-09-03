import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomerAssetsListComponent } from './customer-assets-list.component';

describe('CustomerAssetsListComponent', () => {
  let component: CustomerAssetsListComponent;
  let fixture: ComponentFixture<CustomerAssetsListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CustomerAssetsListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CustomerAssetsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
