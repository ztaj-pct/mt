package com.pct.device.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateDeviceStatusPayload {
	private String deviceUuid;
	private String status;
	private String updatedOn;

}
