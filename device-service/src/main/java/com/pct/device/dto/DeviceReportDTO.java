package com.pct.device.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class DeviceReportDTO {
	
	public String jsonObject;
	private String deviceId;
	private String eventId;
	private String gpsTime;
	private String sequenceNumber;
	private String rawReport;
	private String dateReceived;
	private String timeReceived;

}
