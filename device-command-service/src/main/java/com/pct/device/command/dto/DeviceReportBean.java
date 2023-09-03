package com.pct.device.command.dto;

import java.time.Instant;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
@NoArgsConstructor
public class DeviceReportBean {
	
	private Instant createdDate;

	private Instant CreatedEpoch;

	private String deviceId;

	private Integer sequenceNumber;

	private String rawReport;

	public Integer eventId;

}
