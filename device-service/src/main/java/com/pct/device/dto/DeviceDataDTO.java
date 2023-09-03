package com.pct.device.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class DeviceDataDTO {
	
	private String deviceUuid;
	private String deviceName;
	private String deviceCode;
	private String imei;
	//private List<DeviceDataDTO> deviceList;

}
