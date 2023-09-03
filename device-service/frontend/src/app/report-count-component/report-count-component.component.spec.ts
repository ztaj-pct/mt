import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportCountComponentComponent } from './report-count-component.component';

describe('ReportCountComponentComponent', () => {
  let component: ReportCountComponentComponent;
  let fixture: ComponentFixture<ReportCountComponentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ReportCountComponentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReportCountComponentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
