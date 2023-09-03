package com.pct.common.payload;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InstalledHistroyResponse {

	private Long id;
	private String status;
	private Instant dateStarted;
	private Instant dateEnded;
	private String assinedName;	
	private String imei;
	private String accountNumber;
	private String installCode;
	private String uuid;
	private String appVersion;
	
}
