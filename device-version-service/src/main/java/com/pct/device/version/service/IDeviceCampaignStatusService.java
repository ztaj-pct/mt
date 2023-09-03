package com.pct.device.version.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pct.device.version.payload.CampaignDeviceDetail;

public interface IDeviceCampaignStatusService {
	
	
	Page<CampaignDeviceDetail> getCampaignDeviceDetails(String campaignUUID,Map<String, String> filterValues , Pageable pageable);
 

}
