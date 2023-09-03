package com.pct.device.version.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CampaignSummary {
	
	Long totalGateways;
	Long notStarted;
	Long inProgress;
	Long completed;
	Long onHold;
	Long notEligible;
	Long BaseLineMatch;
	Long offPath;
	Long onPath;
}
