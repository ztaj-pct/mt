package com.pct.device.command.payload;

import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
public class GatewayRequestPayload {
	
	@NonNull
	@JsonProperty("gateway_id")
	private String gatewayId;
	@NonNull
	@JsonProperty("at_command")
	private String atCommand;
	@NonNull
	@JsonProperty("priority")
	private Integer priority;
	
	private String uuid;
	
	@JsonProperty("source")
	private String source;

	@JsonProperty("device_id")
	private String deviceId;

}
