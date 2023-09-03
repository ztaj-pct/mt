package com.pct.device.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pct.common.dto.ForwardRuleResponseDTO;
import com.pct.common.model.Device;
import com.pct.common.model.DeviceForwarding;
import com.pct.common.model.Organisation;
import com.pct.common.model.User;
import com.pct.common.payload.DeviceForwardingRequest;
import com.pct.common.payload.Forwarding;
import com.pct.device.dto.DeviceIgnoreForwardingRuleDTO;
import com.pct.device.dto.ImportForwardingDTO;
import com.pct.device.dto.ImportForwardingResponseDTO;
import com.pct.device.payload.DeviceDataForwardingBulkUploadRequest;
import com.pct.device.payload.DeviceForwardingResponse;

public interface DeviceForwardingService {


	boolean updateDeviceForwarding(DeviceForwarding df) throws Exception;

	boolean deleteDeviceForwarding(String uuid) throws Exception;

	List<DeviceForwardingResponse> getDeviceForwardingById(Long id) throws Exception;

	List<DeviceForwardingResponse> getDeviceForwardingByUuid(String uuid) throws Exception;

	List<DeviceForwardingResponse> getAllDeviceForwarding() throws Exception;

	List<DeviceForwardingResponse> getAllDeviceForwardingByImei(String imei) throws Exception;

	boolean addDeviceForwarding(DeviceForwardingRequest df, String userName) throws Exception;
	
	Map<String, Object> getDeviceForwardingDetailsFromRedis(String imei);
	
	boolean deleteDeviceForwardingByImei(String imei) throws Exception;
	
	Map<String, Set<String>> getIgnoreRuleImeisByCustomerAccountNumber(String customerAccountNumber);

	List<DeviceIgnoreForwardingRuleDTO> getIgnoreRulesUsingDeviceImei(String imei);
	
	Boolean bulkUpdateDeviceForwarding(DeviceDataForwardingBulkUploadRequest request);
	
	void saveExcludedCustomerForwardingRules(List<Forwarding> ignoreForwardingRules, List<Device> devices,
			Organisation endCustomer, Organisation purchasedBy, User user);
	
	void updateExcludeCustomerFrowardingRulesOnRedis(List<Forwarding> ignoreForwardingRules, List<Device> devices);

	Map<String, List<ForwardRuleResponseDTO.DeviceFwdRulResp>> processDeviceForwardingRuleForDevice(
			DeviceDataForwardingBulkUploadRequest request, String gtoken);
	
	void updateExcludeCustomerFrowardingRulesOnRedis(String imei);
	
	List<ImportForwardingResponseDTO> importDeviceForwardingRules(List<ImportForwardingDTO> dtos, String type);
	
}
