package com.pct.device.payload;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InstalledHistoryResponsePayload {
	
	private Long id;
	private String status;
	private String dateStarted;
	private String dateEnded;
	private String assinedName;	
	private String imei;
	private String accountNumber;
	private String installCode;
	private String uuid;
	private String appVersion;

}
