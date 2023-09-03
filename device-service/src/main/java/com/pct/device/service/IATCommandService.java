package com.pct.device.service;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;
import com.pct.common.model.Device;
import com.pct.common.payload.DeviceATCommandReqResponse;
import com.pct.device.Bean.DeviceCommandBean;
import com.pct.device.dto.DeviceReportDTO;
import com.pct.device.dto.GatewayCommandResponse;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.ATCommandRequestPayload;
import com.pct.device.payload.DeviceDetailPayLoad;
import com.pct.device.payload.DeviceDetailsRequest;
import com.pct.device.payload.DeviceResponsePayload;
import com.pct.device.payload.UpdateDeviceStatusPayload;
import com.pct.device.service.device.DeviceDetail;
import com.pct.device.service.device.DeviceReport;

public interface IATCommandService {

	public List<GatewayCommandResponse> getQueuedATCReq(String deviceId);

	DeviceATCommandReqResponse getATCResponse(ATCommandRequestPayload atcRequestPayload, String userName);
	
	Page<DeviceCommandBean> getDeviceCommandWithPagination(String deviceId, Pageable pageable);

	String deletetATCRequest(ATCommandRequestPayload atcRequestPayload, String userId);
}
