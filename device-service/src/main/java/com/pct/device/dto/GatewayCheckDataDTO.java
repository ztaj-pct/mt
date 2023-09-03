package com.pct.device.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class GatewayCheckDataDTO {
	
	private Boolean isAllImei;
	private Boolean isAllMacAddress;
	private Boolean isInvalidIds;
	private Boolean status;
	private List<String> gatewayList;
	private List<String> gatewayListWhichPresentInDb;
	private List<String> invalidIdsList;
	
	private List<String> simNoAlreadyExist;
	private List<String> serialNoAlreadyExist;

}

