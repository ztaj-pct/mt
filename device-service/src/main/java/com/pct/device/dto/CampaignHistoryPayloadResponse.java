package com.pct.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class CampaignHistoryPayloadResponse {

	@JsonProperty("package_payload")
	private CampaignHistoryPackageResponse packagePayload;

	@JsonProperty("device_report")
	private CampaignHistoryDeviceReport deviceReport;
}
