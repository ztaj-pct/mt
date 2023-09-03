package com.pct.device.command.payload;

import java.net.InetAddress;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
public class DeviceCommandRequest {

	public DeviceCommandRequest() {

	}

	public DeviceCommandRequest(String device_id, String at_command, String source, byte[] packet,
			InetAddress inetAddress, int i) {
		super();
		this.deviceId = device_id;
		this.atCommand = at_command;
		this.source = source;
		this.packet = packet;

	}

	public DeviceCommandRequest(String device_id, String at_command, String source, byte[] packet,
			InetAddress inetAddress, int i, String deviceIPAddress, int devicePort) {
		super();
		this.deviceId = device_id;
		this.atCommand = at_command;
		this.source = source;
		this.packet = packet;

	}

	@JsonProperty("gateway_id")
	private String deviceId;
	@JsonProperty("at_command")
	private String atCommand;

	private String source;

	private String priority;

	private Instant last_executed;

	private boolean success;

	private byte[] packet;

	private String uuid;
	
	@JsonProperty("created_epoch")
	private String createdEpoch;
}
