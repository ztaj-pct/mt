package com.pct.device.payload;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
public class BatchDeviceEditPayload {

	private List<String> imei;
	private String productCode;
	private String productName;
	private String can;
	private String usage_status;
	private String deviceType;
	private String installedBy;
	private String purchaseBy;
	private String assetType;
	private String modelYear;
	private String manufacturerName;
	private String serviceNetwork;
	private String countryCode;
	private Boolean isActive;
}
