import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GatewaySummaryListComponent } from './gateway-summary-list.component';

describe('GatewaySummaryListComponent', () => {
  let component: GatewaySummaryListComponent;
  let fixture: ComponentFixture<GatewaySummaryListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GatewaySummaryListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GatewaySummaryListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
