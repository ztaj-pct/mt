package com.pct.organisation.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Location {
	
	private String uuid;
	private String city;
	private String address;
	private String name;
	private String street1;
	private String street2;
	private String state;
	private String county;
	private Float latitude;
	private Float longitude;
	private String locationCode;
	

}
