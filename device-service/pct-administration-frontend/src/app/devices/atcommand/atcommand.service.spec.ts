import { TestBed } from '@angular/core/testing';

import { AtcommandService } from './atcommand.service';

describe('AtcommandService', () => {
  let service: AtcommandService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AtcommandService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
