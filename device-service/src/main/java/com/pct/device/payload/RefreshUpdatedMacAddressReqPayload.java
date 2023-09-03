package com.pct.device.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class RefreshUpdatedMacAddressReqPayload {

	private String sensorUuid;
	private String deviceId;
	private String atCommandUuid;
	private String newSensorId;
}
