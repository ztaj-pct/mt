package com.pct.device.command.dto;

import java.time.Instant;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
@NoArgsConstructor
public class DeviceCommandBean {
	private String deviceId;

	private String atCommand;

	private String source;

	private String priority;

	private Instant last_executed;

	private boolean success;

	private byte[] packet;

	private String createdDate;
	
	private int retryCount;

}
