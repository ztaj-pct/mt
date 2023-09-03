package com.pct.device.version.payload;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PackageSequence {
	private List<PackagePayload> packagePayloadList;
	private CampaignSummary campaignSummary;
}
