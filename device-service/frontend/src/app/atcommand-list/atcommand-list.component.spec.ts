import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ATCommandListComponent } from './atcommand-list.component';

describe('AtcommandComponent', () => {
  let component: ATCommandListComponent;
  let fixture: ComponentFixture<ATCommandListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ATCommandListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ATCommandListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
