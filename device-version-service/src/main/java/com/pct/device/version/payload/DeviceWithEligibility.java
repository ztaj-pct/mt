package com.pct.device.version.payload;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class DeviceWithEligibility implements Serializable {

	private static final long serialVersionUID = 1L;
	
    private String  DEVICE_ID;

    private String  DEVICE_MODEL;

    private java.sql.Timestamp  QA_TIMESTAMP;

    private String  QA_STATUS;

    private String  QA_RESULT;

    private String  OWNER_LEVEL_1;

    private String  OWNER_LEVEL_2;

    private String  OWNER_LEVEL_3;

    private String  OWNER_LEVEL_4;

    private String  DEVICE_USAGE;
    
    private String  device_status_for_campaign;

    private String comments;
    
    private String last_report;
    
    private String deviceInCampaigns;
    
	@JsonProperty("organisation_name")
    private String organisationName;
	
    private String imei;


}
