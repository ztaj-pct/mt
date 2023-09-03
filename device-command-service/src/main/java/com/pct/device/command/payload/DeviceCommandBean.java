package com.pct.device.command.payload;

import java.time.Instant;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class DeviceCommandBean {
	
	private String deviceId;

	private String atCommand;

	private String source;

	private Integer priority;

	private Instant last_executed;

	private boolean success;

	private byte[] packet;

	private String createdDate;
	
	private int retryCount;
	
	private String deviceResponse;
	
	private String status;

	private String uuid;
	
	private String createdBy;
}
