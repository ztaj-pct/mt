import { TestBed } from '@angular/core/testing';

import { CompanyFormServiceService } from './company-form-service.service';

describe('CompanyFormServiceService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: CompanyFormServiceService = TestBed.get(CompanyFormServiceService);
    expect(service).toBeTruthy();
  });
});
