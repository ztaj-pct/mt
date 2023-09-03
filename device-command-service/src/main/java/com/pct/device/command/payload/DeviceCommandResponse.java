package com.pct.device.command.payload;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
@JsonInclude(Include.NON_NULL)
public class DeviceCommandResponse {

	@JsonProperty("device_command")
	private String deviceCommand;

	private String status;

	@JsonProperty("device_id")
	private String deviceId;

	@JsonInclude(JsonInclude.Include.ALWAYS)
	@JsonProperty("at_commands")
	private List<String> atCommands;

	public DeviceCommandResponse() {

	}

}
