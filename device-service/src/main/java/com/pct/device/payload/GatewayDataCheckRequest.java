package com.pct.device.payload;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class GatewayDataCheckRequest {
	
	String isProductIsApprovedForAsset;
	List<String> deviceList;
	List<Map<String, Object>> deviceDetails;
	
}
