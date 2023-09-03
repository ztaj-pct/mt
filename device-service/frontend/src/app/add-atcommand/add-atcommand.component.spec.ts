import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AddATCommandComponent } from './add-atcommand.component';

describe('AddAtcommandComponent', () => {
  let component: AddATCommandComponent;
  let fixture: ComponentFixture<AddATCommandComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AddATCommandComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddATCommandComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
