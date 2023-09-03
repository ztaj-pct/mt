package com.pct.device.command.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
@ToString
public class RedisDeviceCommand {
	@JsonProperty("at_command")
	private String command;
	private Integer priority;
	private String uuid;
	private String createdEpoch;
	private String createdBy;
	private String source;
	private String serverIP ;
	private String serverPort;
	private String commandId;
	

}
