import { TestBed } from '@angular/core/testing';

import { GatewayListService } from './gateway-list.service';

describe('GatewayListService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: GatewayListService = TestBed.get(GatewayListService);
    expect(service).toBeTruthy();
  });
});
