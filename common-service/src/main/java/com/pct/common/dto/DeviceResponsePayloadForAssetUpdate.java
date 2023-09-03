package com.pct.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponsePayloadForAssetUpdate {
	private Long deviceId;
	private String imei;
	private String deviceUuid;
	private Long organisationId;
	private String organisationUuid;
	private String organisationAccNo;
	
}
