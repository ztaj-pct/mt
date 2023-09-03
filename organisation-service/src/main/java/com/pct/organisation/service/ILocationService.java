package com.pct.organisation.service;

import java.util.List;

import com.pct.common.dto.LocationDTO;
import com.pct.common.model.Location;
import com.pct.organisation.payload.LocationRequestPayload;

public interface ILocationService {
	
	public Boolean addOrganizationLocations(LocationRequestPayload locationRequestPayload);
	
	public Boolean updateOrganizationLocations(LocationDTO locationDTO);
	
	public List<LocationDTO> getOrganizationLocations(Long organisationId);
	
	public Location getLocation(Long locationId);
	
	public Boolean deleteLocation(Long locationId);
}
