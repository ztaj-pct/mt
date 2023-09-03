package com.pct.device.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.pct.common.payload.DeviceATCommandReqResponse;
import com.pct.device.Bean.DeviceCommandBean;
import com.pct.device.dto.GatewayCommandResponse;
import com.pct.device.ms.repository.IDeviceCommandMsRepository;
import com.pct.device.payload.ATCommandRequestPayload;
import com.pct.device.service.IATCommandService;
import com.pct.device.service.device.DeviceCommand;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;

@Service
public class ATCommandServiceImpl implements IATCommandService {

	Logger logger = LoggerFactory.getLogger(ATCommandServiceImpl.class);

	@Autowired
	private RestUtils restUtils;

	@Autowired
	private IDeviceCommandMsRepository deviceCommandRepository;

	@Autowired
	private BeanConverter beanConverter;

	@Override
	public List<GatewayCommandResponse> getQueuedATCReq(String deviceId) {
		logger.info("Inside getQueuedATCReq and fetching deviceId " + deviceId);
		List<GatewayCommandResponse> atcQueueReport = restUtils.getATCQueueRequest(deviceId);
		return atcQueueReport;
	}

	@Override
	public DeviceATCommandReqResponse getATCResponse(ATCommandRequestPayload atcRequestPayload, String userName) {
		logger.info("Inside getATCResponse and fetching payload " + atcRequestPayload);
		DeviceATCommandReqResponse atcResponse = restUtils.getATCResponse(atcRequestPayload);
		return atcResponse;
	}
	@Override
	public String deletetATCRequest(ATCommandRequestPayload atcRequestPayload, String userId) {
		logger.info("Inside getATCResponse and fetching payload " + atcRequestPayload);
		String atcResponse = restUtils.deleteATCResponse(atcRequestPayload);
		return atcResponse;
	}

	@Override
	public Page<DeviceCommandBean> getDeviceCommandWithPagination(String deviceId, Pageable pageable) {
		List<DeviceCommandBean> deviceCommandBeanList = new ArrayList<>();
		Page<DeviceCommandBean> deviceCommandBeanDetailList = new PageImpl<>(deviceCommandBeanList);
		Page<DeviceCommand> deviceCommandList = null;
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		logger.info(
				"before getting response from getDeviceCommandWithPagination method from DeviceCommand service find All method :");
		// Specification<DeviceCommand> spc =
		// DeviceCommandSpecification.getDeviceCommandSpecification(deviceId,filterValues);
		deviceCommandList = deviceCommandRepository.findGatewayCommandByDeviceId(deviceId,pageable);
		stopWatch.stop();
		logger.info(
				"after getting response from getDeviceCommandWithPagination method from DeviceCommand service find All method :");
		StopWatch stopWatchBean = new StopWatch();
		stopWatchBean.start();
		logger.info(
				"before getting response from getDeviceCommandWithPagination method from DeviceCommand service find All bean converter method :");
		if (deviceCommandList != null && deviceCommandList.getContent() != null
				&& deviceCommandList.getContent().size() > 0) {
			for (DeviceCommand device : deviceCommandList.getContent()) {
				deviceCommandBeanList.add(beanConverter.convertDeviceCommandToDeviceCommand(device));
			}
		}
		if (deviceCommandBeanList != null && deviceCommandBeanList.size() > 0) {
			deviceCommandBeanDetailList = new PageImpl<>(deviceCommandBeanList);
		}
		stopWatchBean.stop();
		logger.info(
				"after getting response from getDeviceCommandWithPagination method from DeviceCommand service find All bean converter method :");
		return deviceCommandBeanDetailList;
	}
}
