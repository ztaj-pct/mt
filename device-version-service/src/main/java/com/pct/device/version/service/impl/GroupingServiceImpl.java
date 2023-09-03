package com.pct.device.version.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.pct.common.dto.MsDeviceRestResponse;
import com.pct.device.version.constant.CampaignStatus;
import com.pct.device.version.constant.CampaignStepDeviceStatus;
import com.pct.device.version.constant.DeviceStatusForCampaign;
import com.pct.device.version.constant.GroupingConstants;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.pct.device.version.model.Campaign;
import com.pct.device.version.model.DeviceCampaignStatus;
import com.pct.device.version.model.Grouping;
import com.pct.device.version.payload.UpdateCampaignPayload;
import com.pct.device.version.repository.IDeviceCampaignStatusRepository;
import com.pct.device.version.repository.IGroupingRepository;
import com.pct.device.version.service.IGroupingService;

@Service
public class GroupingServiceImpl implements IGroupingService {

	Logger logger = LoggerFactory.getLogger(GroupingServiceImpl.class);

	@Autowired
	private IGroupingRepository groupingRepository;
	@Autowired
	IDeviceCampaignStatusRepository deviceCampaignStatusRepository;
	@Override
	public Grouping saveTargetImei(String imeis, String customer, String notEligibleImeis, String excludedImei
			) {

		Grouping gr = new Grouping();
		gr.setExcludedImei(excludedImei);
	
		// case for DEFINED_LIST
		if (customer.equalsIgnoreCase("IMEI")) {
			gr.setTargetValue(imeis);
			gr.setGroupingName(GroupingConstants.GROUPING_NAME_CUSTOM);
			gr.setGroupingType(GroupingConstants.GROUPING_TYPE_IMEI);
			// case for UNRESTRICTED
		} else if (customer.equalsIgnoreCase("ALL")) {

// 			Page<MsDeviceRestResponse> msDevices = restUtils.getDeviceDataFromMS(customer, 0 ,100000, null,"asc");
//
//			List<String> allImeis = msDevices.stream().map(MsDeviceRestResponse::getImei).collect(Collectors.toList());
//			String imeisStr = allImeis.toString();
//			imeisStr = imeisStr.replace("[", "");
//			imeisStr = imeisStr.replace("]", "");
//
// 			gr.setTargetValue(imeisStr);
			gr.setGroupingName(GroupingConstants.GROUPING_TARGET_VALUE_ALL);
			gr.setGroupingType(GroupingConstants.GROUPING_TARGET_VALUE_ALL);
			// case for RESTRICTED
		} else {
// 			Page<MsDeviceRestResponse> msDevices = restUtils.getDeviceDataFromMS(customer, 0 ,100000, null,"asc");
//
//			List<String> allImeis = msDevices.stream().map(MsDeviceRestResponse::getImei).collect(Collectors.toList());
//			String imeisStr = allImeis.toString();
//			imeisStr = imeisStr.replace("[", "");
//			imeisStr = imeisStr.replace("]", "");
//
// 			gr.setTargetValue(imeisStr);
			gr.setGroupingName(customer);
			gr.setGroupingType(GroupingConstants.GROUPING_TYPE_CUSTOMER);
		}
		String groupingUuid = "";
		boolean isGroupingUuidUnique = false;
		while (!isGroupingUuidUnique) {
			groupingUuid = UUID.randomUUID().toString();
			Grouping byUuid = groupingRepository.findByUuid(groupingUuid);
			if (byUuid == null) {
				isGroupingUuidUnique = true;
			}
		}
		gr.setUuid(groupingUuid);
		gr = groupingRepository.save(gr);
		return gr;
	}

	@Override
	public Grouping updateTargetImei(UpdateCampaignPayload campaignToUpdate, Campaign campData, String msgUUId) {
		String imeis = campaignToUpdate.getImeiList();
		String customer = campaignToUpdate.getCustomerName();

		Grouping grouping = campData.getGroup();
		// if (customer == null) {
		if (campData.getGroup().getGroupingType().equalsIgnoreCase(GroupingConstants.GROUPING_TYPE_IMEI)) {
			if (!campData.getCampaignStatus().equals(CampaignStatus.NOT_STARTED)) {

				ArrayList<String> existingImeis = new ArrayList<>(
						Arrays.asList(campData.getGroup().getTargetValue().split(",")));
				logger.info(msgUUId + "existingImeis "+existingImeis);
				ArrayList<String> newImeis = new ArrayList<>(Arrays.asList(imeis.split(",")));
				logger.info(msgUUId + "newImeis "+newImeis);
				String existingImeisCommaString = campData.getGroup().getTargetValue();
				logger.info(msgUUId + "existingImeisCommaString "+existingImeisCommaString);
				if (existingImeis.containsAll(newImeis)) {
					existingImeisCommaString = String.join(",", newImeis);
				} else {
					if (campData.getCampaignStatus().equals(CampaignStatus.FINISHED)) {
						campaignToUpdate.setCampaignStatus(CampaignStatus.IN_PROGRESS.getValue());
						campaignToUpdate.setIsActive(true);
					}
				}
				logger.info(msgUUId + "existingImeisCommaString "+existingImeisCommaString);

				imeis = existingImeisCommaString + "," + imeis;
				logger.info(msgUUId + "imeis    "+imeis);

				String imeis1 = String.join(",", new HashSet<>(Arrays.asList(imeis.split(","))));
				logger.info(msgUUId + "imeis 1   "+imeis1);
				imeis =imeis1;

			} else {
				if (campaignToUpdate.getIsReplace() != null && !campaignToUpdate.getIsReplace()) {
					
					String existingImeisCommaString = campData.getGroup().getTargetValue();
					logger.info(msgUUId + "existingImeisCommaString 1"+existingImeisCommaString);
					imeis = existingImeisCommaString + "," + imeis;
					imeis = String.join(",", new HashSet<>(Arrays.asList(imeis.split(","))));
				}
				
			}
			logger.info(msgUUId + "Updating imeis "+imeis);
			grouping.setTargetValue(imeis);
			
			
		}
		// grouping.setGroupingName("Custom");
		// grouping.setGroupingType("imei");
		// } else {
		// grouping.setTargetValue("ALL");
		// grouping.setGroupingName(customer);
		// grouping.setGroupingType("Customer");
		// }

		Set<String> updateRemovedImeis = new HashSet<String>();
		List<String> newRemovedImeis = campaignToUpdate.getImeisToBeRemoved();
		if (newRemovedImeis != null && newRemovedImeis.size() > 0) {
			updateRemovedImeis.addAll(newRemovedImeis);
		}
		if (campData.getGroup().getRemovedImei() != null) {
			updateRemovedImeis.addAll(new ArrayList<>(Arrays.asList(campData.getGroup().getRemovedImei().split(","))));

		}
		if (updateRemovedImeis.size() > 0) {
			String removeIimeis = String.join(",", updateRemovedImeis);
			grouping.setRemovedImei(removeIimeis);
		}		
		
		Set<String> updateExcludedImeis = new HashSet<String>();
		String newExcludedImeiString = campaignToUpdate.getExcludedImeis();
			
		List<String> newExcludedImeis = null;
		if(newExcludedImeiString != null)
		{
			newExcludedImeis = new ArrayList<>(Arrays.asList(newExcludedImeiString.split(",")));
		}
		
		if (newExcludedImeis != null && newExcludedImeis.size() > 0) {
			updateExcludedImeis.addAll(newExcludedImeis);
		}
		if (campData.getGroup().getExcludedImei() != null) {
			updateExcludedImeis.addAll(new ArrayList<>(Arrays.asList(campData.getGroup().getExcludedImei().split(","))));

		}
		if (updateExcludedImeis.size() > 0) {
			String updatedIimeis = String.join(",", updateExcludedImeis);
			grouping.setExcludedImei(updatedIimeis);
		}
		grouping = groupingRepository.save(grouping);
		updateStatus(campaignToUpdate,  campData,msgUUId);
		return grouping;
	}
	
	private void updateStatus(UpdateCampaignPayload campaignToUpdate, Campaign campData, String msgUUId)
	{
		
		
		List<String> newRemovedImeis = campaignToUpdate.getImeisToBeRemoved();
		logger.info(msgUUId + "remove list "+newRemovedImeis);
		
		if(newRemovedImeis != null && newRemovedImeis.size() > 0)
		{
			List<DeviceCampaignStatus> deviceCampaignList =  deviceCampaignStatusRepository.findByDeviceListAndCampaignUUID(newRemovedImeis, campData.getUuid());
			for(DeviceCampaignStatus deviceCampaignStatus : deviceCampaignList)
			{
				deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.REMOVED.getValue());
			}
			deviceCampaignStatusRepository.saveAll(deviceCampaignList);
		}
		String newExcludedImeiString = campaignToUpdate.getExcludedImeis();
		logger.info(msgUUId + "exckuded list "+newRemovedImeis);
		if(newExcludedImeiString != null)
		{
			List<String> newExcludedImeiList =	new ArrayList<>(Arrays.asList(newExcludedImeiString.split(",")));
			List<DeviceCampaignStatus> deviceCampaignList =  deviceCampaignStatusRepository.findByDeviceListAndCampaignUUID(newExcludedImeiList, campData.getUuid());
			for(DeviceCampaignStatus deviceCampaignStatus : deviceCampaignList)
			{
				deviceCampaignStatus.setRunningStatus(DeviceStatusForCampaign.EXCLUDED.getValue());
			}
			deviceCampaignStatusRepository.saveAll(deviceCampaignList);
		}
		
	}

}
