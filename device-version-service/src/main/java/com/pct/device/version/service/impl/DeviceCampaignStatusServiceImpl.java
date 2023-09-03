package com.pct.device.version.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.pct.device.version.constant.CampaignStepDeviceStatus;
import com.pct.device.version.model.CampaignStepDeviceDetail;
import com.pct.device.version.model.DeviceCampaignStatus;
import com.pct.device.version.model.LatestDeviceMaintenanceReport;
import com.pct.device.version.model.MultipleCampaignDevice;
import com.pct.device.version.payload.CampaignDeviceDetail;
import com.pct.device.version.repository.ICampaignStepDeviceDetailRepository;
import com.pct.device.version.repository.ICampaignStepRepository;
import com.pct.device.version.repository.IDeviceCampaignStatusRepository;
import com.pct.device.version.repository.ILatestDeviceMaintenanceReportRepository;
import com.pct.device.version.service.IDeviceCampaignStatusService;
import com.pct.device.version.specification.DeviceCampaignStatusSpecification;
import com.pct.device.version.util.BeanConverter;


@Service
public class DeviceCampaignStatusServiceImpl implements IDeviceCampaignStatusService {

	Logger logger = LoggerFactory.getLogger(DeviceCampaignStatusServiceImpl.class);
	Logger analysisLog = LoggerFactory.getLogger("analytics");

	@Autowired
	private BeanConverter beanConverter;

	@Autowired
	private DeviceCampaignStatusSpecification deviceCampaignStatusSpecification;

	@Autowired
	private ICampaignStepDeviceDetailRepository deviceDetailRepository;
	@Autowired
	private IDeviceCampaignStatusRepository deviceCampaignStatusRepository;
	@Autowired
	private ICampaignStepRepository campaignStepRepository;
	@Autowired
	private ILatestDeviceMaintenanceReportRepository latestDeviceMaintenanceReportRepository;
	

	@Override
	public Page<CampaignDeviceDetail> getCampaignDeviceDetails(String campaignUUID, Map<String, String> filterValues,
			Pageable pageable) {
		
		Specification<DeviceCampaignStatus> specification = deviceCampaignStatusSpecification
				.getSpecification(campaignUUID, filterValues);
		Long maxStepInCamp = campaignStepRepository.findLastStepByCampaignUuid(campaignUUID).getStepOrderNumber();
		Page<DeviceCampaignStatus> deviceCampaignStatusList = deviceCampaignStatusRepository.findAll(specification,
				pageable);
		Map<String, List<CampaignStepDeviceDetail>> imeiStepData = new HashMap<String, List<CampaignStepDeviceDetail>>();
		Map<String, List<MultipleCampaignDevice>> OnHoldForCampaignData = new HashMap<String, List<MultipleCampaignDevice>>();
		
		List<CampaignStepDeviceDetail> allStepDetail = deviceDetailRepository.findByCampaignUuid(campaignUUID,
				CampaignStepDeviceStatus.FAILED);

		if (allStepDetail != null) {
			imeiStepData = allStepDetail.stream().collect(Collectors.groupingBy(w -> w.getDeviceId()));
		}
		Map<String , List<DeviceCampaignStatus>> deviceCampaignStatusMap = deviceCampaignStatusList.getContent().stream().collect(Collectors.groupingBy(w -> w.getDeviceId()));
		List<String> imeiList = new ArrayList(deviceCampaignStatusMap.keySet());
		
		
	//List<String> imeiList = 	 deviceCampaignStatusList.stream().map(e->e.getDeviceId()).collect(Collectors.toList());
	/*
	 * List<CampaignStepDeviceDetail> allStepDetail =
	 * deviceDetailRepository.getDeviceCampaign(campaignUUID, imeiList); if
	 * (allStepDetail != null) { imeiStepData =
	 * allStepDetail.stream().collect(Collectors.groupingBy(w -> w.getDeviceId()));
	 * }
	 */
		List<CampaignDeviceDetail> deviceCampaignStatusListToCampaignDeviceDetailList = beanConverter
				.deviceCampaignStatusListToCampaignDeviceDetailList(deviceCampaignStatusList.getContent(), imeiStepData,
						maxStepInCamp.intValue());

		return new PageImpl<>(deviceCampaignStatusListToCampaignDeviceDetailList, pageable,
				deviceCampaignStatusList.getTotalElements());
	}
}