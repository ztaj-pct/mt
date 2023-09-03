import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TlvColumndefsComponent } from './tlv-columndefs.component';

describe('TlvColumndefsComponent', () => {
  let component: TlvColumndefsComponent;
  let fixture: ComponentFixture<TlvColumndefsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TlvColumndefsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TlvColumndefsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
