package com.pct.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;


@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class CampaignHistoryPackageResponse {
	
	 @JsonProperty("uuid")
	 private String uuid;
	 
	 @JsonProperty("package_name")
     private String packageName;
	 
	 @JsonProperty("bin_version")
	 private String binVersion;
	 
	 @JsonProperty("app_version")
     private String appVersion;
	 
	 @JsonProperty("mcu_version")
	 private String mcuVersion;
	 
	 @JsonProperty("ble_version")
     private String bleVersion;
	 
	 @JsonProperty("config1")
	 private String config1;
	 
	 @JsonProperty("config2")
	 private String config2;
	 
	 @JsonProperty("config3")
	 private String config3;
	 
	 @JsonProperty("config4")
	 private String config4;
	 
	 @JsonProperty("config1_crc")
     private String config1Crc;
	 
	 @JsonProperty("config2_crc")
     private String config2Crc;
	 
	 @JsonProperty("config3_crc")
     private String config3Crc;
	 
	 @JsonProperty("config4_crc")
     private String config4Crc;
	 
	 @JsonProperty("is_deleted")
	 private boolean isDeleted;
	 
	 @JsonProperty("is_used_in_campaign")
	 private boolean isUsedInCampaign;
	 
	 @JsonProperty("created_by")
     private String createdBy;
	 
	 @JsonProperty("updated_by")
     private String updatedBy;
	 
	 @JsonProperty("created_at")
     private String createdAt;
	 
	 @JsonProperty("updated_at")
     private String updatedAt;
	 
	 @JsonProperty("device_type")
	 private String deviceType;
	 
	 @JsonProperty("lite_sentry_hardware")
     private String liteSentryHardware;
	 
	 @JsonProperty("lite_sentry_app")
	 private String liteSentryApp;
	 
	 @JsonProperty("lite_sentry_boot")
	 private String liteSentryBoot;
	 
	 @JsonProperty("microsp_mcu")
     private String microspMcu;
	 
	 @JsonProperty("microsp_app")
     private String microspApp;
	 
	 @JsonProperty("cargo_maxbotix_hardware")
	 private String cargoMaxbotixHardware;
	 
	 @JsonProperty("cargo_maxbotix_firmware")
     private String cargoMaxbotixFirmware;
	 
	 @JsonProperty("cargo_riot_hardware")
	 private String cargoRiotHardware;
	 
	 @JsonProperty("cargo_riot_firmware")
     private String cargoRiotFirmware;

}
