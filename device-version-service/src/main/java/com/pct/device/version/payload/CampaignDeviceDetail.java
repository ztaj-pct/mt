package com.pct.device.version.payload;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.device.service.device.DeviceReport;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CampaignDeviceDetail {

	private String imei;
	private String productName;
	private String customerName;
	private String deviceStatusForCampaign;
	private String lastReport;
	private String comments;
	private List<DeviceStepStatus> deviceStepStatus;
	private List<CampaignHyperLink> campaignHyperLink;
	private List<DeviceCampaignHistory> deviceCampaignHistory;
	private DeviceReport deviceReport;
	private String installedFlag;
	private String organisationName;
	private String customerId;
	private boolean isRemoved;
	

	
}
