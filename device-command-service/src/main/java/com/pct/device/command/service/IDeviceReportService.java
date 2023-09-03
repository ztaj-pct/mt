package com.pct.device.command.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pct.common.payload.DeviceATCommandReqResponse;
import com.pct.device.command.dto.DeviceReportBean;
import com.pct.device.command.entity.DeviceCommand;

public interface IDeviceReportService {
	
	public Page<DeviceReportBean> getRawReportWithPagination(String deviceId, Pageable pageable);
	
	public DeviceATCommandReqResponse getATCommandResponse(String uuid);
	
	public DeviceATCommandReqResponse getATCommandLatestResponse(String deviceId);

}
