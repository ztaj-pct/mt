package com.pct.installer.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SensorInstallInstructionDto {
	private String sensorProductName;
	private Integer stepSequence;
	private String installInstructionUUID;
	private String sensorProductCode;
}