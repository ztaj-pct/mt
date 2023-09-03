package com.pct.organisation.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pct.common.dto.LocationDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.model.Location;
import com.pct.common.model.Organisation;
import com.pct.organisation.exception.BadRequestException;
import com.pct.organisation.payload.LocationRequestPayload;
import com.pct.organisation.repository.IOrganisationRepository;
import com.pct.organisation.repository.LocationRepository;
import com.pct.organisation.service.ILocationService;
import com.pct.organisation.util.BeanConvertor;
import com.pct.organisation.util.RestUtils;

@Service
public class LocationServiceImpl implements ILocationService {

	@Autowired
	private IOrganisationRepository organisationRepository;

	@Autowired
	private LocationRepository locationRepository;
	
	@Autowired
	BeanConvertor beanConvertor;
	
	@Autowired
	private RestUtils restUtils;

	@Override
	public Boolean addOrganizationLocations(LocationRequestPayload locationRequestPayload) {

		if (locationRequestPayload.getOrganisationId() == null) {
			throw new BadRequestException("organisation id cannot be null");
		}
		Organisation organisation = organisationRepository.findById(locationRequestPayload.getOrganisationId()).get();
		if (organisation == null) {
			throw new BadRequestException("organisation not found by given id");
		}
		for (LocationDTO locationDTO : locationRequestPayload.getLocationList()) {
			Location location = new Location();
			location.setLocationName(locationDTO.getLocationName());
			location.setStreetAddress(locationDTO.getStreetAddress());
			location.setCity(locationDTO.getCity());
			location.setState(locationDTO.getState());
			location.setZipCode(locationDTO.getZipCode());
			location.setOrganisation(organisation);
			location = locationRepository.save(location);
		}
		return true;
	}

	@Override
	public List<LocationDTO> getOrganizationLocations(Long organisationId) {
		Organisation organisation = organisationRepository.findById(organisationId).get();
		if (organisation == null) {
			throw new BadRequestException("organisation not found by given id");
		}
		List<Location> locations = locationRepository.findLocationByOrganisationUuid(organisation.getUuid());
		return beanConvertor.convertLocationEntityTOLocationDto(locations);
	}

	@Override
	public Boolean updateOrganizationLocations(LocationDTO locationDTO) {

		if (locationDTO == null || locationDTO.getId() == null) {
			throw new BadRequestException("location can not be null");
		}
		Location location = locationRepository.findById(locationDTO.getId()).get();
		if (location == null) {
			throw new BadRequestException("location not found");
		}
		
		ResponseDTO responseDTO = restUtils.getIsUserAssoicatedWithLocationFromAuthService(location.getId());
		if(!location.getUsers().isEmpty() || responseDTO.getStatus() == true) {
			return false;
		}
		location.setLocationName(locationDTO.getLocationName());
		location.setStreetAddress(locationDTO.getStreetAddress());
		location.setCity(locationDTO.getCity());
		location.setState(locationDTO.getState());
		location.setZipCode(locationDTO.getZipCode());
		location = locationRepository.save(location);
		return true;
	}

	@Override
	public Boolean deleteLocation(Long locationId) {
		if (locationId == null) {
			throw new BadRequestException("location Id can not be null");
		}
		Location location = locationRepository.findById(locationId).get();
		ResponseDTO responseDTO = restUtils.getIsUserAssoicatedWithLocationFromAuthService(locationId);
		if(!location.getUsers().isEmpty() || responseDTO.getStatus() == true) {
			return false;
		}
		locationRepository.deleteById(locationId);
		return true;
	}

	@Override
	public Location getLocation(Long locationId) {

		if (locationId == null) {
			throw new BadRequestException("location Id can not be null");
		}
		Location location = locationRepository.findById(locationId).get();
		location.setOrganisation(null);
		location.setUsers(null);
		return location;
	}

}
