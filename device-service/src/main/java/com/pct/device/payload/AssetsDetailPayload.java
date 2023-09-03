package com.pct.device.payload;

import java.time.Instant;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
//import com.pct.common.constant.OrganisationType;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
public class AssetsDetailPayload {

	private Long assetUniqId;
	private String companyName;
	private String assetType;
	private String assetID;
	private String assetName;
	private String vin;
	private String model;
	private String year;
	private String manufacturerName;
	private Instant installedDate;
	private String noOfTires;
	private String noOfAxel;
	private String externalLength;
	private String assetNickName;
	private String tag;
	private String doorType;

}
