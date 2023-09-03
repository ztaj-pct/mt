package com.pct.device.dto;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class CampaignHistoryResponse {
	
	 private String campaign_name;
     private String litesentry_hardware;
     private String litesentry_app;
     private String liteSentry_boot;
     private String campaing_status;
     private String ble_version;
     private String config1CIV;
     private String config2CIV;
     private String config3CIV;
     private String config4CIV;
     private String config5CIV;
     private String app_version;
     private String bin_version;
     private String config1CRC;
     private String config2CRC;
     private String config3CRC;
     private String config4CRC;
     private String config5CRC;
     private String deviceType;
}
