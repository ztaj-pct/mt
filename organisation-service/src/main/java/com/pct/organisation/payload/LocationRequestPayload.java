package com.pct.organisation.payload;

import java.util.List;

import com.pct.common.dto.LocationDTO;
import com.pct.common.model.Organisation;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LocationRequestPayload {
	
	private List<LocationDTO> locationList;
	
	private Organisation organisation;
	
	private Long organisationId;

}
