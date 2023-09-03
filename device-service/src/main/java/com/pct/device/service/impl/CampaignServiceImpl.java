package com.pct.device.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.pct.device.Bean.DeviceCommandBean;
import com.pct.device.dto.CampaignHistoryDeviceResponse;
import com.pct.device.dto.CampaignHistoryPayloadResponse;
import com.pct.device.dto.CampaignInstallDeviceResponse;
import com.pct.device.dto.CurrentCampaignResponse;
import com.pct.device.dto.GatewayCommandResponse;
import com.pct.device.ms.repository.IDeviceCommandMsRepository;
import com.pct.device.payload.ATCommandRequestPayload;
import com.pct.device.service.IATCommandService;
import com.pct.device.service.ICampaignService;
import com.pct.device.service.device.DeviceCommand;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;

@Service
public class CampaignServiceImpl implements ICampaignService {

	Logger logger = LoggerFactory.getLogger(CampaignServiceImpl.class);

	@Autowired
	private RestUtils restUtils;



	@Override
	public  List<CampaignHistoryDeviceResponse> getDeviceCampaignHistoryByImei(String imei) {
		List<CampaignHistoryDeviceResponse> deviceCampaignDetails= restUtils.getDeviceCampaignHistoryByImei(imei);
		return deviceCampaignDetails;
	}

	public List<CampaignInstallDeviceResponse> getCampaignInstalledDevice(String imei) {
		return restUtils.getCampaignInstalledDevice(imei);
	}

	public List<CampaignHistoryPayloadResponse> campaignHistory(String imei) {
		return restUtils.getCampaignHistory(imei);
	}

	@Override
	public CurrentCampaignResponse getCurrentCampaign(String imei) {
		return restUtils.getCurrentCampaign(imei);
	}
}
