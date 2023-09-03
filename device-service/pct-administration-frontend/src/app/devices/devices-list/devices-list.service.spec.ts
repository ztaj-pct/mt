import { TestBed } from '@angular/core/testing';
import { DevicesListService } from './devices-list.service';

describe('DevicesListService', () => {
  let service: DevicesListService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DevicesListService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
