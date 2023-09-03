package com.pct.device.payload;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class BeaconRequestPayload {
	
	private String productCode;
	private String productName;
	private String appVersion;
	private String mcuVersion;
	private String binVersion;
	private String bleVersion;
	private String config1;
	private String epicorOrderNumber;
	private String can;
	private String son;
	private String macAddress;
    
}
