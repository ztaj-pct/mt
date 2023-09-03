package com.pct.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class CampaignInstallDeviceResponse {

	@JsonProperty("device_ID")
	private String deviceId;

	@JsonProperty("first_POWER_UP_TIMESTAMP_AFTER_QA")
	private String firstPowerUpTimeStampAfterQa;

	@JsonProperty("latest_MAINTENANCE_QA")
	private boolean latestMaintenanceQa;

	@JsonProperty("latest_MAINTENANCE_QA_TIMESTAMP")
	private String latestMaintenanceQaTimeStamp;
	
	@JsonProperty("is_POWER_UP_AFTER_QA")
	private boolean isPowerUpAfterQa;
	
	@JsonProperty("power_UP_REPORT_COUNTER")
	private int powerUpReportCounter;
	
	@JsonProperty("is_INSTALLED_FOR_CAMPAIGN")
	private boolean isInstalledForCampaign;
	
	@JsonProperty("is_INSTALLED_FOR_CAMPAIGN_TIMESTAMP")
	private String isInstalledForCampaignTimeStamp;
	
	@JsonProperty("is_EXISTING_DB_DEVICE")
	private boolean isExistingDbDevice;
	
	@JsonProperty("record_ID")
	private int recordId;
	
	@JsonProperty("qa_STATUS")
	private boolean qaStatus;
	
	@JsonProperty("qa_TIMESTAMP")
	private String qaTimeStamp;
	
	@JsonProperty("last_UPDATED_AT")
	private String lastUpdatedAt;

}
