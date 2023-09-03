package com.pct.device.payload;

import java.time.Instant;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
public class SensorSubDetailPayload {
	
	private String position;
	private String type;
	private String uuid;
	private String sensorId;
	private String sensorUUID;
	private String sensorPressure;
	private String sensorTemperature;
	private String vendor;
	private String status;
	private String atCommandStatus;
	private String atCommandUuid;
	private String newSensorId;
	private Instant updatedOn;
	private Instant createdOn;

}
