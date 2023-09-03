import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AtcommandComponent } from './atcommand.component';

describe('AtcommandComponent', () => {
  let component: AtcommandComponent;
  let fixture: ComponentFixture<AtcommandComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AtcommandComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AtcommandComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
