package com.pct.device.service;

import java.util.List;

import com.pct.device.dto.CampaignHistoryDeviceResponse;
import com.pct.device.dto.CurrentCampaignResponse;



public interface ICampaignService {

	List<CampaignHistoryDeviceResponse> getDeviceCampaignHistoryByImei(String imei);
	CurrentCampaignResponse getCurrentCampaign(String imei);

	
}
