package com.pct.device.payload;

import java.util.List;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AssestDissociationRequest {

	@JsonProperty("gateway_id")
	@Size(max = 50, message = "Input size too long")
	private String gatewayId;

	@JsonProperty("asset_id")
	@Size(max = 50, message = "Input size too long")
	private String AssetId;

	
	@JsonProperty("event_datetime")
	@Size(max = 50, message = "Input size too long")
	private String eventDateTime;


	@Override
	public String toString() {
		return "AssestDissociationRequest [gatewayId=" + gatewayId + ", AssetId=" + AssetId + ", eventDateTime="
				+ eventDateTime + "]";
	}


}
