package com.pct.device.version.service;

import com.pct.device.version.constant.CampaignStatus;
import com.pct.device.version.exception.DeviceInMultipleCampaignsException;
import com.pct.device.version.payload.ExecuteCampaignRequest;



/**
 * @author dhruv
 *
 */
public interface ICampaignExecutionService {


	String processCampaign(ExecuteCampaignRequest executeCampaignRequest, String msgUuid) throws DeviceInMultipleCampaignsException;

	String manageCampaignStatus(String campaignUuid, String userName, String campaignStatus, Long pauseLimit);
	
}
