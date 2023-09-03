package com.pct.device.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CurrentCampaignResponse {
	private String campaignName;
	private String campaignUuid;
	private List<VersionMigrationDetailDTO> versionMigrationDetailDtoList;
 }
