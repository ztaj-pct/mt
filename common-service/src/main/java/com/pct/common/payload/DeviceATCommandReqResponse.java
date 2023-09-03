package com.pct.common.payload;

import java.time.Instant;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DeviceATCommandReqResponse {

	private Long id;

//	private Instant createdDate;
//
//	private Instant CreatedEpoch;
//
//	private Instant updatedDate;

	private String uuid;

	private String deviceId;

	private String atCommand;

	private String source;

	private String priority;

//	private Instant sentTimestamp;

	private boolean is_success;

	private String deviceIp;

	private int devicePort;

	private String deviceResponse;

	private int retryCount;

	private String rawReport;

	private String serverIp;

	private int serverPort;

	private String responseServerIp;

	private int responseServerPort;
	
	private String status;
	
	private String createdBy;

	private String updatedBy;
	
	private String deviceRawResponse;
	
	private String createdOn;
	
	private String updatedOn;
}
