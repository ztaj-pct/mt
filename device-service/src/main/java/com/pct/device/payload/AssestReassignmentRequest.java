package com.pct.device.payload;

import javax.persistence.Column;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AssestReassignmentRequest {

	@JsonProperty("gateway_id")
	@Size(max = 50, message = "Input size too long")
	private String gatewayId;

	@JsonProperty("old_asset_id")
	@Size(max = 50, message = "Input size too long")
	private String oldAssetId;

	@JsonProperty("new_asset_id")
	@Size(max = 50, message = "Input size too long")
	private String newAssetId;

	@JsonProperty("event_datetime")
	@Size(max = 50, message = "Input size too long")
	private String eventDateTime;


	@Column(name = "asset_type")
	@Size(max = 50, message = "Input size too long")
	private String assetType;

	@Override
	public String toString() {
		return "AssestReassignmentRequest [gatewayId=" + gatewayId + ", oldAssetId=" + oldAssetId + ", newAssetId="
				+ newAssetId + ", eventDateTime=" + eventDateTime + ", asset_type=" + assetType + "]";
	}

	


}
