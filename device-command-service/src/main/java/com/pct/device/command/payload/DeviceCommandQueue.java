package com.pct.device.command.payload;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
public class DeviceCommandQueue {
	@JsonProperty("gateway_command")
	List<RedisDeviceCommand> redisDeviceCommand = new ArrayList<RedisDeviceCommand>();
	long lastRequestEpoch;
	String lastCommandSent;
	boolean hasPendingCommand;
	boolean lastCommandExecuted;
	long lastResponseEpoch;

	public String toJson() {

		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String returnJson = objectMapper.writeValueAsString(this);
			return returnJson;
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
