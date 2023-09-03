package com.pct.device.payload;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class SensorRequestPayload {
	
	private String productCode;
	private String productName;
	private String bleVersion;
	private String epicorOrderNumber;
	private String can;
	private String son;
	private String macAddress;
	private List<SensorSubDetailRequestPayload> sensorSubDetails;
    
}
