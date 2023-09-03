package com.pct.installer.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SensorReasonCodeDto {

	private String sensorProductName;
	private String code;
	private String value;
	private String issueType;
	private String reasonCodeUuid;
	private String sensorProductCode;

}
