package com.pct.device.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
public class AssetDeviceAssociationPayLoad {

	@JsonProperty("vin")
	private String vin;
	
	@JsonProperty("assetId")
	private String assetId;
	
	@JsonProperty("imei")
	private String imei;

}
