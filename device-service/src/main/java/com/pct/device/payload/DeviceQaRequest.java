package com.pct.device.payload;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

//@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class DeviceQaRequest {

	private String qaStatus;
	private String uuid;
	private String qaResult;
	private Timestamp qaDate;
	private String imei;
	
	
	
	
}