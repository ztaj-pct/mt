import { TestBed } from '@angular/core/testing';

import { AddATCommandService } from './add-atcommand.service';

describe('AddAtcommandService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: AddATCommandService = TestBed.get(AddATCommandService);
    expect(service).toBeTruthy();
  });
});
