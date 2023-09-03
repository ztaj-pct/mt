import { TestBed } from '@angular/core/testing';

import { ATCommandService } from './atcommand.service';

describe('AtcommandService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ATCommandService = TestBed.get(ATCommandService);
    expect(service).toBeTruthy();
  });
});
