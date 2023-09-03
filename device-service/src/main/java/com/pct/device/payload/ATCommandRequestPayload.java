package com.pct.device.payload;

import java.util.List;

import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ATCommandRequestPayload {
	
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
}
