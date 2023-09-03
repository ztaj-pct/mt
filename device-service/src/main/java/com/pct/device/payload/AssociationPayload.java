package com.pct.device.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
public class AssociationPayload {

	private String assetId;
	private String assetType;
	private String deviceId;
	private String installedDate;
	private String make;
	private String vin;
	private String gatewayEligibility;
	private CompanyPayload companyPayload;
}
