package com.pct.device.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.DeviceType;
import com.pct.common.constant.GatewayStatus;
import com.pct.common.constant.GatewayType;
import com.pct.common.constant.IOTType;
import com.pct.device.Bean.DeviceBean;
import com.pct.device.dto.GatewayCheckDataDTO;
import com.pct.device.dto.GatewayUpdateDTO;
import com.pct.device.dto.MessageDTO;
import com.pct.device.dto.SensorSectionDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.AssetsStatusPayload;
import com.pct.device.payload.BeaconDetailsRequest;
import com.pct.device.payload.GatewayBeanForMobileApp;
import com.pct.device.payload.GatewayBulkUploadRequest;
import com.pct.device.payload.GatewayBulkUploadRequestWithDeviceForwarding;
import com.pct.device.payload.GatewayDataCheckRequest;
import com.pct.device.payload.GatewayDetailPayLoad;
import com.pct.device.payload.GatewayPayload;
import com.pct.device.payload.GatewaySensorAssociation;
import com.pct.device.payload.GatewaySensorPayload;
import com.pct.device.payload.GatewaySummaryPayload;
import com.pct.device.payload.GatewayUploadRequest;
import com.pct.device.payload.ShipmentDetailsRequest;
import com.pct.device.payload.UpdateGatewayRequest;
import com.pct.device.payload.UpdateMacAddressRequest;

public interface IGatewayService {

	Boolean addGatewayDetail(GatewayUploadRequest gatewayUploadRequest, String userName) throws DeviceException;

	Page<GatewayPayload> getGatewayWithPagination(String accountNumber, String imei, String uuid, DeviceStatus status,
			Map<String, String> filterValues, String macAddress, Pageable pageable, String timeOfLastDownload);

	boolean deleteGatewayDetail(String can, String mac, String uuid);

	Boolean updateGatewayDetail(GatewayDetailPayLoad gatewayDetailPayload,  String userName);
	
	Boolean uploadBulkGateway(GatewayBulkUploadRequest gatewayBulkUploadRequest,String userId);
	
	GatewayCheckDataDTO checkGatewayListData(GatewayDataCheckRequest gatewayDataCheckRequest);

	List<GatewaySensorPayload> getGatewaySensorDetails(String imei, String deviceUuid);

	List<GatewayPayload> getGatewayDetails(String accountNumber, String uuid, String gatewayId);

	Boolean associateGatewaySensor(GatewaySensorAssociation gatewaysensorRequest, String userName);
	
	List<AssetsStatusPayload> getAssetsStatusDetails(String imei, String deviceUuid, String userName);
	
	Boolean bulkDeleteForGateways(List<String> listOfUuid);
	
	Boolean updateGateway(UpdateGatewayRequest updateGatewayRequest, String userName);
	
	List<SensorSectionDTO> getSensorSectionList();
	
	Page<GatewaySummaryPayload> getCustomerGatewaySummary(Pageable pageable, String userName, Map<String, String> filterValues);
	 
	 public void getDeviceDetailsForCsv(Page<GatewaySummaryPayload> payloadList, HttpServletResponse response)
				throws IOException;

	Boolean uploadBulkGatewayWithDeviceForwarding(
			GatewayBulkUploadRequestWithDeviceForwarding gatewayBulkUploadRequestWithDeviceForwarding, String userName);
	
	public List<GatewayUpdateDTO> uploadBulkGateway(GatewayBulkUploadRequestWithDeviceForwarding gatewayBulkUploadRequestWithDeviceForwarding, String userName);

	Map<String, List<String>> saveShipmentDetails(ShipmentDetailsRequest shipmentDetailsRequest);

	Map<String, List<String>> saveBeaconDetails(BeaconDetailsRequest beaconDetailsRequest, Long userId);

	Boolean updateGatewayMacAddress(UpdateMacAddressRequest updateMacAddressRequest, Long userId);

	Boolean markInstalledToDevices(List<String> listOfUuid, Long userId);

	Boolean resetIndividualDevice(String gatewayUuid, Long userId);

	List<DeviceBean> getGateway(String accountNumber, String imei, String gatewayUuid, DeviceStatus gatewayStatus,
			IOTType type, String macAddress);

	Page<GatewayBeanForMobileApp> getGatewayWithPagination(String accountNumber, String imei, String gatewayUuid,
			DeviceStatus gatewayStatus, IOTType type, String macAddress, Pageable pageable,
			MessageDTO<Page<GatewayBeanForMobileApp>> messageDto, String timeOfLastDownload);
}
