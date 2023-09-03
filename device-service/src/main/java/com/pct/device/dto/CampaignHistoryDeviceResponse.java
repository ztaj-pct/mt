package com.pct.device.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class CampaignHistoryDeviceResponse {
	
	 @JsonProperty("campaing_name")
	 private String campaingName;
	 
	 @JsonProperty("last_step")
     private String lastStep;
	 
	 @JsonProperty("last_step_time")
     private String lastStepTime;
	 
	 @JsonProperty("last_step_status")
     private String lastStepStatus;
	 
	 @JsonProperty("campaing_status")
     private String campaingStatus;
	 
	 @JsonProperty("ble_version")
     private String bleVersion;
	 
	 @JsonProperty("config1_civ")
     private String config1Civ;
	 
	 @JsonProperty("config2_civ")
     private String config2Civ;
	 
	 @JsonProperty("config3_civ")
     private String config3Civ;
	 
	 @JsonProperty("config4_civ")
     private String config4Civ;
	 
	 @JsonProperty("app_version")
     private String appVersion;
	 
	 @JsonProperty("bin_version")
     private String binVersion;
	 
	 @JsonProperty("config1_crc")
     private String config1Crc;
	 
	 @JsonProperty("config2_crc")
     private String config2Crc;
     
     @JsonProperty("config3_crc")
     private String config3Crc;
     
     @JsonProperty("config4_crc")
     private String config4Crc;
     
}
