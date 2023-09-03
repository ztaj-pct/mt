package com.pct.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LocationDTO {
	
	@JsonProperty("id")
	private Long id;
	
	@JsonProperty("location_name")
	private String locationName;
	
	@JsonProperty("street_address")
	private String streetAddress;
	
	@JsonProperty("city")
	private String city;
	
	@JsonProperty("state")
	private String state;
	
	@JsonProperty("zip_code")
	private String zipCode;

}
