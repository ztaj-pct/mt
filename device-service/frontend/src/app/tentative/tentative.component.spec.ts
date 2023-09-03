import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TentativeComponent } from './tentative.component';

describe('TentativeComponent', () => {
  let component: TentativeComponent;
  let fixture: ComponentFixture<TentativeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TentativeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TentativeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
