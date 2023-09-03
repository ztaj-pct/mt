package com.pct.device.version.dto;

import com.pct.device.service.device.DeviceReport;
import com.pct.device.version.constant.CampaignStepDeviceStatus;

import lombok.Data;

@Data

public class DeviceCampaignStatusDTO {
	CampaignStepDeviceStatus campaignRunningStatus;
	String currentCampaignUUID ;
	boolean atCommandFound ;
	String lastPendingStepUUID ;
	String lastSuccessStepUUID ;
	String currentStepUUID;
	String deviceId;
	DeviceReport deviceReport;
	String isEligible;
	String problemStatus;
	String problemComment;
	String comment;
	long lastStepOrderNumber;
	String lastStepExecutionDate;
	String customerNam;
	String customerId;
	
	
}
