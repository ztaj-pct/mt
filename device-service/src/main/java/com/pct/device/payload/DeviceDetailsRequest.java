package com.pct.device.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.constant.IOTType;
import com.pct.common.model.Organisation;

import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class DeviceDetailsRequest {

	private String imei;
	private String productCode;
	private String productName;
	private IOTType type;
	private String quantityShipped;
	private String appVersion;
	private String mcuVersion;
	private String binVersion;
	private String bleVersion;
	private String other1Version;
	private String other2Version;
	private String config1;
	private String config2;
	private String config3;
	private String config4;
	private String epicorOrderNumber;
	private String can;
	private String son;
	private String macAddress;
	private CellularDetailPayload cellularPayload;
	private String usage_status;
	private String qaStatus;
	private String deviceType;
	private String ownerLevel2;
	
}
