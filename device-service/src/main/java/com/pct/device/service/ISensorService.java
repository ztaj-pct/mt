package com.pct.device.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pct.common.constant.DeviceStatus;
import com.pct.device.dto.MessageDTO;
import com.pct.device.dto.SensorDataDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.RefreshUpdatedMacAddressReqPayload;
import com.pct.device.payload.RetriveDeviceSensorRequest;
import com.pct.device.payload.SensorDetailPayLoad;
import com.pct.device.payload.SensorPayLoad;
import com.pct.device.payload.SensorRequestPayload;
import com.pct.device.payload.UpdateMacAddressRequest;

public interface ISensorService {

	Boolean addSensorDetail(SensorRequestPayload sensorRequestRequest, String userName) throws DeviceException;

	Page<SensorPayLoad> getSensorWithPagination(String accountNumber, String uuid, DeviceStatus status, String mac,
			Map<String, String> filterValues, Pageable pageable);

	boolean deleteSensorDetail(String can, String uuid);

	Boolean updateSensorDetail(SensorDetailPayLoad sensorDetailPayload, String userName);

	List<SensorPayLoad> getSensorDetails(String accountNumber, String uuid);

	List<SensorDataDTO> getSensorList();

	public MessageDTO updateSensorDetailsMacAddress(Map<String, Object> sensorDetails);
	
	public MessageDTO refreshUpdatedSensor(RefreshUpdatedMacAddressReqPayload sensorDetails);

	public MessageDTO getAndAddSensorDetails(RetriveDeviceSensorRequest retriveDeviceSensorRequest);
	
	public MessageDTO getAndAddLatestSensorDetails(RetriveDeviceSensorRequest retriveDeviceSensorRequest);

	Boolean updateSensorMacAddress(UpdateMacAddressRequest updateMacAddressRequest,  String userName);
}
