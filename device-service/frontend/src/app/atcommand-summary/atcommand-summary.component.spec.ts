import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AtcommandSummaryComponent } from './atcommand-summary.component';

describe('AtcommandSummaryComponent', () => {
  let component: AtcommandSummaryComponent;
  let fixture: ComponentFixture<AtcommandSummaryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AtcommandSummaryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AtcommandSummaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
