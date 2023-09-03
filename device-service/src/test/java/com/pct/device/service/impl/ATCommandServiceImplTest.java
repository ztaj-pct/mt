package com.pct.device.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StopWatch;

import com.pct.common.payload.DeviceATCommandReqResponse;
import com.pct.device.Bean.DeviceCommandBean;
import com.pct.device.dto.GatewayCommandResponse;
import com.pct.device.ms.repository.IDeviceCommandMsRepository;
import com.pct.device.payload.ATCommandRequestPayload;
import com.pct.device.service.device.DeviceCommand;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.RestUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class ATCommandServiceImplTest {

	@Mock
	private RestUtils restUtils;

	@Mock
	private IDeviceCommandMsRepository deviceCommandRepository;

	@Mock
	private BeanConverter beanConverter;

	@InjectMocks
	private ATCommandServiceImpl service;

	@Test
	public void getQueuedATCReq() {

		String deviceId = "12";

		GatewayCommandResponse gatewayCommandResponse = new GatewayCommandResponse();
		gatewayCommandResponse.setAt_command("at_command");
		gatewayCommandResponse.setCreated_by("created_by");
		gatewayCommandResponse.setCreated_epoch("created_epoch");
		gatewayCommandResponse.setPriority(23);
		gatewayCommandResponse.setUuid("4d06b4b9-e9e8-47b5-bc5e-fe40330e14be");

		List<GatewayCommandResponse> atcQueueReport = new ArrayList();
		atcQueueReport.add(gatewayCommandResponse);

		Mockito.when(restUtils.getATCQueueRequest(deviceId)).thenReturn(atcQueueReport);
		List<GatewayCommandResponse> queuedATCReq = service.getQueuedATCReq(deviceId);
		assertNotNull(queuedATCReq);
	}

	@Test
	public void getATCResponse() {

		String userName = "userName";

		ATCommandRequestPayload atcRequestPayload = new ATCommandRequestPayload();
		atcRequestPayload.setUuid("4d06b4b9-e9e8-47b5-bc5e-fe40330e14be");
		atcRequestPayload.setAtCommand("atCommand");
		atcRequestPayload.setGatewayId("gatewayId");
		atcRequestPayload.setPriority(2);

		Mockito.when(restUtils.getATCResponse(atcRequestPayload)).thenReturn(new DeviceATCommandReqResponse());
		DeviceATCommandReqResponse atcResponse = service.getATCResponse(atcRequestPayload, userName);
		assertNotNull(atcResponse);
	}

	@Test
	public void deletetATCRequest() {

		String userId = "23";

		ATCommandRequestPayload atcRequestPayload = new ATCommandRequestPayload();
		atcRequestPayload.setUuid("4d06b4b9-e9e8-47b5-bc5e-fe40330e14be");
		atcRequestPayload.setAtCommand("atCommand");
		atcRequestPayload.setGatewayId("gatewayId");
		atcRequestPayload.setPriority(2);

		Mockito.when(restUtils.deleteATCResponse(atcRequestPayload)).thenReturn(userId);
		String deletetATCRequest = service.deletetATCRequest(atcRequestPayload, userId);
		assertNotNull(deletetATCRequest);
	}

	@Test
	public void getDeviceCommandWithPagination() {

		Long Id = 44L;
		String deviceId = "23";
		Pageable pageable = PageRequest.of(0, 10);
		byte[] packet = { 1, 2, 3 };

		DeviceCommandBean deviceCommandBean = new DeviceCommandBean();
		deviceCommandBean.setAtCommand("command");
		deviceCommandBean.setCreatedDate("createdDate");
		deviceCommandBean.setDeviceId(deviceId);
		deviceCommandBean.setDeviceResponse("response");
		deviceCommandBean.setLast_executed(Instant.now());
		deviceCommandBean.setPacket(packet);
		deviceCommandBean.setPriority(2);
		deviceCommandBean.setRetryCount(0);
		deviceCommandBean.setSource("source");
		deviceCommandBean.setStatus("status");
		deviceCommandBean.setSuccess(true);
		deviceCommandBean.setUuid("4d06b4b9-e9e8-47b5-bc5e-fe40330e14be");

		List<DeviceCommandBean> deviceCommandBeanList = new ArrayList<>();
		deviceCommandBeanList.add(deviceCommandBean);

		Page<DeviceCommandBean> deviceCommandBeanDetailList = new PageImpl<>(deviceCommandBeanList);

		DeviceCommand deviceCommand = new DeviceCommand();
		deviceCommand.setAt_command("at_command");
		deviceCommand.setCreated_date("created_date");
		deviceCommand.setCreated_epoch(Instant.now());
		deviceCommand.setDevice_id(deviceId);
		deviceCommand.setDevice_ip("127.0.0.1");
		deviceCommand.setDevice_port(3306);
		deviceCommand.setDevice_response("ok");
		deviceCommand.setId(Id);
		deviceCommand.setPriority("2");
		deviceCommand.setRaw_report(
				"7d010015115004700885112b2a373d101a500117bc2a373d1007146a5e04de7cdf396c694653d000026246e2100901374d00110000105af00104007a007ae6100100e6210104e614028000e6200104e6270104e62a0104e6120400000000e629010c");
		deviceCommand.setResponse_server_ip("127.0.0.10");
		deviceCommand.setResponse_server_port(1506);
		deviceCommand.setRetry_count(0);
		deviceCommand.setSent_timestamp(Instant.now());
		deviceCommand.setServer_ip("127.0.0.30");
		deviceCommand.setServer_port(8014);
		deviceCommand.setSource("source");
		deviceCommand.setStatus("Active");
		deviceCommand.setSuccess(true);
		deviceCommand.setUpdated_date(Instant.now());
		deviceCommand.setUuid("4d06b4b9-e9e8-47b5-bc5e-fe40330e14be");

		List<DeviceCommand> deviceCommandList = new ArrayList();
		deviceCommandList.add(deviceCommand);

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		Page<DeviceCommand> deviceCommandListPage = new PageImpl<>(deviceCommandList);

		Mockito.when(deviceCommandRepository.findGatewayCommandByDeviceId(deviceId, pageable))
				.thenReturn(deviceCommandListPage);

		Page<DeviceCommandBean> deviceCommandWithPagination = service.getDeviceCommandWithPagination(deviceId,
				pageable);
		assertNotNull(deviceCommandWithPagination);
	}
}