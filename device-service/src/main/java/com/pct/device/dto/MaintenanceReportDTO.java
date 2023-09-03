package com.pct.device.dto;

import java.time.Instant;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.model.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
//@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class MaintenanceReportDTO {
	
	//private Instant createdDate;
	//private Times serviceTime;
	private String assetId;
	private String deviceId;
	private String sensorType;
	private String oldSensorId;
	private String newSensorId;
	private String validationTime;
	private String resolutionType;
	private String workOrder;
	private String organization;
	private String maintenanceLocation;
	private String sensorUuid;
	private String macaddress;
	private String position;
}
