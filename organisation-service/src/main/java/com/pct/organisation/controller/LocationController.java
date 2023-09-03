package com.pct.organisation.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.dto.LocationDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.model.Location;
import com.pct.organisation.payload.LocationRequestPayload;
import com.pct.organisation.service.ILocationService;

@RestController
@RequestMapping("/location")
public class LocationController {
	
	Logger logger = LoggerFactory.getLogger(LocationController.class);
	
	@Autowired
	private ILocationService locationService;
	
	
	@PostMapping()
	public ResponseEntity<ResponseDTO> saveOrganizationLocations(@RequestBody LocationRequestPayload locationRequestPayload) {
		logger.info("Inside saveOrganizationLocations Method");
		try {
			Boolean status = locationService.addOrganizationLocations(locationRequestPayload);
			logger.info("Exiting from saveOrganizationLocations Method");
			return new ResponseEntity<>(new ResponseDTO(status, "Successfully Added Organization's Locations"),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Getting Exception while saving organisation's locations", exception);
			return new ResponseEntity<>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PutMapping()
	public ResponseEntity<ResponseDTO> updateOrganizationLocations(@RequestBody LocationDTO locationDTO) {
		logger.info("Inside updateOrganizationLocations Method");
		try {
			Boolean status = locationService.updateOrganizationLocations(locationDTO);
			logger.info("Exiting from updateOrganizationLocations Method");
			if(status) {
				return new ResponseEntity<>(new ResponseDTO(status, "Successfully Added Organization's Locations"),
						HttpStatus.OK);
			} else {
				return new ResponseEntity<>(new ResponseDTO(status, "Location associated with user cannot be updated"),
						HttpStatus.BAD_REQUEST);
			}
			
		} catch (Exception exception) {
			logger.error("Getting Exception while updating organisation's location", exception);
			return new ResponseEntity<>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/get-locations")
	public ResponseEntity<List<LocationDTO>> getLocations(@RequestParam(value = "organisationId",required=true) Long organisationId) {
		try {
			logger.info("Request to get organisation's locations by organisationId");
			List<LocationDTO> locations = locationService.getOrganizationLocations(organisationId);
			return new ResponseEntity<>(locations, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting organisation's locations", exception);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/byId")
	public ResponseEntity<Location> getLocationByIs(@RequestParam(value = "locationId",required=true) Long locationId) {
		try {
			logger.info("Request to get organisation's location by locationId");
			Location location = locationService.getLocation(locationId);
			return new ResponseEntity<>(location, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting organisation's location", exception);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@DeleteMapping()
	public ResponseEntity<ResponseDTO> removeLocation(@RequestParam(value = "locationId",required=true) Long locationId) {
		try {
			logger.info("Request to delete organisation's location by locationId");
			Boolean status = locationService.deleteLocation(locationId);
			if(status) {
				return new ResponseEntity<>(new ResponseDTO(status, "Successfully Added Organization's Locations"),
						HttpStatus.OK);
			} else {
				return new ResponseEntity<>(new ResponseDTO(status, "This location cannot be removed because it is associated with one or more users."),
						HttpStatus.OK);
			}
			
		} catch (Exception exception) {
			logger.error("Exception occurred while deleting organisation's location", exception);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
