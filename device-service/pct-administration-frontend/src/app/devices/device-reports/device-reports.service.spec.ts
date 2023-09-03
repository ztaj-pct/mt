import { TestBed } from '@angular/core/testing';

import { DeviceReportsService } from './device-reports.service';

describe('DeviceReportsService', () => {
  let service: DeviceReportsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DeviceReportsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
