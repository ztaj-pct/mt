package com.pct.device.version.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pct.device.version.constant.CampaignStatus;
import com.pct.device.version.model.Campaign;
import com.pct.device.version.model.Grouping;
import com.pct.device.version.payload.CampaignPayload;
import com.pct.device.version.payload.PackagePayload;
import com.pct.device.version.payload.SaveCampaignRequest;
import com.pct.device.version.payload.SavePackageRequest;
import com.pct.device.version.payload.UpdateCampaignPayload;



/**
 * @author dhruv
 *
 */
public interface IGroupingService {

	Grouping saveTargetImei(String imeiList, String customerUuid, String notEligibleDevices, String excludedImei);

	Grouping updateTargetImei(UpdateCampaignPayload campaignToUpdate, Campaign campaign , String msgUUId);




}
