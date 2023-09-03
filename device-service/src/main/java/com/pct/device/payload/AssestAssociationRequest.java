package com.pct.device.payload;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AssestAssociationRequest {

	@JsonProperty("gateway_id")
	@Size(max = 50, message = "Input size too long")
	private String gatewayId;

	@JsonProperty("asset_id")
	@Size(max = 50, message = "Input size too long")
	private String AssetId;

	@JsonProperty("event_datetime")
	@Size(max = 50, message = "Input size too long")
	private String eventDateTime;

	@JsonProperty("asset_type")
	@Size(max = 50, message = "Input size too long")
	private String assetType;
	
	@JsonProperty("overwrite_if_exists")
	private Boolean overWriteIfExists;

	@Override
	public String toString() {
		return "AssestAssociationRequest [gatewayId=" + gatewayId + ", AssetId=" + AssetId + ", eventDateTime="
				+ eventDateTime + ", assetType=" + assetType + ", overwriteIfExists=" + overWriteIfExists + "]";
	}

	

	

}
