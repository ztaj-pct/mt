package com.pct.device.payload;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class DeviceUploadRequest {
	
	CompanyPayload company;
	private Boolean isAllImei;
	private Boolean isAllMacAddress;
	private Boolean status;
	private String eligibleDevice;
	private String salesforceOrderId;
	private List<String> gatewayList;
    
}
