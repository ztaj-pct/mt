package com.pct.device.payload;

import java.sql.Timestamp;

import com.pct.common.model.Device;

import lombok.Data;
@Data
public class DeviceQaResponse {

	   
		private String qaStatus;
		private String qaResult;
		private String uuid;
		private String deviceId;
	    private Timestamp qaDate;
}