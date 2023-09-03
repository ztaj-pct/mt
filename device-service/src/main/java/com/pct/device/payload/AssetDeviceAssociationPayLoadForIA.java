package com.pct.device.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AssetDeviceAssociationPayLoadForIA {

	private String imei;

	private String vin;
	private String assetId;
	private String eligibleGateway;
	private String category;
	private String year;
	private String manufacturer;
	private String accountNumber;
	private String status;
	private Boolean isVinValidated;
	private String comment;

}
