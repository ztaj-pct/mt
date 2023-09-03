package com.pct.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class CampaignHistoryDeviceReport {
	
	 @JsonProperty("config1CRC")
	 private String config1CRC;
	 
	 @JsonProperty("config1CIV")
     private String config1CIV;
	 
	 @JsonProperty("config2CRC")
	 private String config2CRC;
	 
	 @JsonProperty("config2CIV")
     private String config2CIV;
	 
	 @JsonProperty("config3CRC")
	 private String config3CRC;
	 
	 @JsonProperty("config3CIV")
     private String config3CIV;
	 
	 @JsonProperty("config4CRC")
	 private String config4CRC;
	 
	 @JsonProperty("config4CIV")
     private String config4CIV;
	 
	 @JsonProperty("config5CIV")
	 private String config5CIV;
	 
	 @JsonProperty("config5CRC")
     private String config5CRC;
	 
	 @JsonProperty("isDeviceUpgradeEligibleForCampaign")
     private String isDeviceUpgradeEligibleForCampaign;
	 
	 @JsonProperty("deviceInstalledForCampaignDT")
	 private String deviceInstalledForCampaignDT;
	 
	 @JsonProperty("isDeviceInstalledForCampaign")
     private String isDeviceInstalledForCampaign;
	 
	 @JsonProperty("customer")
	 private String customer;
	 
	 @JsonProperty("configurationDesc")
     private String configurationDesc;
	 
	 @JsonProperty("maxbotixStatus")
	 private String maxbotixStatus;
	 
	 @JsonProperty("liteSentryBoot")
     private String liteSentryBoot;
	 
	 @JsonProperty("liteSentryApp")
     private String liteSentryApp;
	 
	 @JsonProperty("liteSentryHw")
	 private String liteSentryHw;
	 
	 @JsonProperty("liteSentryStatus")
     private String liteSentryStatus;
	 
	 @JsonProperty("device_type")
	 private String deviceType;
	 
	 @JsonProperty("deviceUpgradeEligibleForCampaignDT")
     private String deviceUpgradeEligibleForCampaignDT;
	
	 @JsonProperty("app_SW_VERSION")
     private String appSwVersion;
	 
	 @JsonProperty("baseband_SW_VERSION")
	 private String basebandSwVersion;
	 
	 @JsonProperty("riothardware")
     private String riothardware;
	 
	 @JsonProperty("riotFirmware")
	 private String riotFirmware;
	 
	 @JsonProperty("riotStatus")
     private String riotStatus;
	
	 @JsonProperty("steApp")
     private String steApp;
	 
	 @JsonProperty("steMCU")
	 private String steMCU;
	 
	 @JsonProperty("steStatus")
     private String steStatus;
	 
	 @JsonProperty("maxbotixhardware")
	 private String maxbotixhardware;
	 
	 @JsonProperty("maxbotixFirmware")
     private String maxbotixFirmware;
	 
	 @JsonProperty("report_ID")
     private String reportId;
	 
	 @JsonProperty("ble_VERSION")
	 private String bleVersion;
	 
	 @JsonProperty("server_INFO_STR")
     private String serverInfoStr;
	 
	 @JsonProperty("device_ID")
	 private String deviceId;
	 
	 @JsonProperty("extender_VERSION")
     private String extenderVersion;
	
}
