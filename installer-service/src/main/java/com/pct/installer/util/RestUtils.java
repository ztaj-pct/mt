package com.pct.installer.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pct.common.dto.AssetDTO;
import com.pct.common.dto.DeviceResponsePayloadForAssetUpdate;
import com.pct.common.dto.OrganisationDtoForInventory;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.dto.UserDTO;
//import com.pct.common.dto.ResponseDTOForUDPCommand;
import com.pct.common.exception.InterServiceRestException;
import com.pct.common.model.Asset;
//import com.pct.common.model.AssetGatewayXref;
import com.pct.common.model.AssetSensorXref;
import com.pct.common.model.Attribute;
import com.pct.common.model.AttributeValue;
import com.pct.common.model.Device;
import com.pct.common.model.InstallHistory;
import com.pct.common.model.Organisation;
import com.pct.common.model.User;
import com.pct.common.payload.AssetSensorXrefPayload;
import com.pct.common.payload.AssetsPayload;
import com.pct.common.payload.DeviceSensorxrefPayload;
import com.pct.common.payload.GatewayDetailsBean;
import com.pct.common.payload.InstallationStatusGatewayRequest;
import com.pct.common.payload.SaveAssetGatewayXrefRequest;
import com.pct.common.payload.SensorUpdateRequest;
import com.pct.common.payload.UpdateAssetForGatewayRequest;
import com.pct.common.payload.UpdateAssetToDeviceForInstallationRequest;
import com.pct.common.payload.UpdateGatewayAssetStatusRequest;
import com.pct.common.util.Logutils;
import com.pct.installer.dto.AttributeResponseDTO;
import com.pct.installer.dto.MessageDTO;
import com.pct.installer.feign.clients.AuthServiceClient;
import com.pct.installer.feign.clients.DeviceServiceFeignClient;
import com.pct.installer.feign.clients.OrganisationServiceFeignClient;
import com.pct.installer.payload.AssetsStatusPayload;
import com.pct.installer.payload.DeviceResponsePayload;
import com.pct.installer.payload.ElasticFinishInstallRequest;

/**
 * @author Abhishek on 24/04/20
 */

@Service
public class RestUtils {

	Logger logger = LoggerFactory.getLogger(RestUtils.class);

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	DeviceServiceFeignClient deviceServiceFeignClient;

	@Autowired
	AuthServiceClient authServiceClient;

	@Autowired
	OrganisationServiceFeignClient organisationServiceFeignClient;

	@Autowired
	private BeanConverter beanConverter;
	@Value("${service.gateway.serviceId}")
	private String gatewayServiceId;
	@Value("${elastic.url.finish-install}")
	private String elasticFinishInstallUrl;
	@Value("${elastic.token.finish-install}")
	private String elasticFinishInstallToken;
//    
//    @Value("${endpoint.sendUDPCommand}")
//    private String sendUDPCommandURL;

	public static final String className = "RestUtils";

//    public JobSummaryResponse getJobSummaryFromDeviceService(JobSummaryRequest jobSummaryRequest) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/job-summary";
//        ResponseEntity<JobSummaryResponse> jobSummaryResponseResponseEntity = restTemplate.postForEntity(url, jobSummaryRequest, JobSummaryResponse.class);
//        if (jobSummaryResponseResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            return jobSummaryResponseResponseEntity.getBody();
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", jobSummaryResponseResponseEntity.getStatusCode());
//        }
//    }
	// ---------------------------------------------Aamir
	// Start------------------------------------------------------//
	// "/device/core/gateway/can-mac?can=" + can + "&mac=" + mac;
	public Device getGatewayByMACAndCan(String logUUId, String mac, String can) {
		Logutils.log("getGatewayByMACAndCan", logUUId,
				" Before calling [/device/can-mac] from device service", logger);

		ResponseEntity<ResponseBodyDTO<List<DeviceResponsePayload>>> assetResponseEntity = deviceServiceFeignClient
				.getGatewayByMACAndCan(logUUId, mac, can);
		Logutils.log("getGatewayByMACAndCan", logUUId,
				" After calling [/device/can-mac] from device service", logger);
		if (assetResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			Device device = new Device();
			BeanUtils.copyProperties(assetResponseEntity.getBody().body.get(0), device);
			return device;
		} else {
			throw new InterServiceRestException("Http call not successful", assetResponseEntity.getStatusCode());
		}
	}

	// "/device/imei/" + imei;
	public Device getGatewayByImei(String logUUId, String imei) {
		Logutils.log("getGatewayByImei", logUUId, " Before calling [/device] from device service", logger);

		ResponseEntity<ResponseBodyDTO<List<DeviceResponsePayload>>> gatewayResponseEntity = deviceServiceFeignClient
				.getGatewayByImei(logUUId, imei);

		Logutils.log("getGatewayByImei", logUUId, " After calling [/device] from device service", logger);
		if (gatewayResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			Device device = new Device();
			BeanUtils.copyProperties(gatewayResponseEntity.getBody().getBody().get(0), device);
			return device;
		} else {
			throw new InterServiceRestException("Http call not successful", gatewayResponseEntity.getStatusCode());
		}
	}

	// "/asset/uuid/" + assetUuid;
	public Asset getAssetByAssetUUID(String logUUId, String assetUuid) {
		Logutils.log("getAssetByAssetUUID", logUUId, " Before calling [/asset/uuid/{uuid}] from device service", logger);

		ResponseEntity<ResponseBodyDTO<List<AssetsPayload>>> assetResponseEntity = deviceServiceFeignClient
				.getAssetByAssetUUID(logUUId, assetUuid);
		Logutils.log("getAssetByAssetUUID", logUUId, " After calling [/asset/uuid/{uuid}] from device service", logger);
		Asset asset = new Asset();
		BeanUtils.copyProperties(assetResponseEntity.getBody().body.get(0), asset);
		if (assetResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			return asset;
		} else {
			return null;
		}
	}

	// "/user/get/" + userId;
	public User getUserFromAuthService(String logUUId, Long userId) {
		Logutils.log("getUserFromAuthService", logUUId, " Before calling [/user/get/] from Auth service", logger);
		String methodName = "getUserFromAuthService";
		ResponseEntity<MessageDTO<UserDTO>> userResponseEntity = authServiceClient.getUserFromAuthService(userId);
		Logutils.log("getUserFromAuthService", logUUId, " After calling [/user/get/] from Auth service", logger);
		if (userResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			User user = objectMapper.convertValue(userResponseEntity.getBody().getBody(), User.class);

			return user;
		} else {
			throw new InterServiceRestException("Http call not successful", userResponseEntity.getStatusCode());
		}
	}

	// "/device/update-asset";
	public Device updateAssetForGateway(String logUUId, String gatewayUuid, String assetUuid) {
		Logutils.log("updateAssetForGateway", logUUId, " Before calling [/device/update-asset] from device service",
				logger);

		UpdateAssetForGatewayRequest updateAssetForGatewayRequest = new UpdateAssetForGatewayRequest();
		updateAssetForGatewayRequest.setAssetUuid(assetUuid);
		updateAssetForGatewayRequest.setGatewayUuid(gatewayUuid);
		ResponseEntity<ResponseBodyDTO<List<DeviceResponsePayload>>> responseEntity = deviceServiceFeignClient
				.updateAssetForGateway(logUUId, gatewayUuid, assetUuid);

		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			Device device = new Device();
			BeanUtils.copyProperties(responseEntity.getBody().body.get(0), device);
			return device;
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}
	}

	public DeviceResponsePayloadForAssetUpdate updateAssetForGatewayV1(String logUUId, String gatewayUuid,
			String assetUuid) {
		Logutils.log("updateAssetForGateway", logUUId, " Before calling [/device/update-asset-new] from device service",
				logger);

		UpdateAssetForGatewayRequest updateAssetForGatewayRequest = new UpdateAssetForGatewayRequest();
		updateAssetForGatewayRequest.setAssetUuid(assetUuid);
		updateAssetForGatewayRequest.setGatewayUuid(gatewayUuid);
		ResponseEntity<ResponseBodyDTO<DeviceResponsePayloadForAssetUpdate>> responseEntity = deviceServiceFeignClient
				.updateAssetForGatewayV1(logUUId, gatewayUuid, assetUuid);

		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			DeviceResponsePayloadForAssetUpdate deviceResponsePayloadForAssetUpdate = new DeviceResponsePayloadForAssetUpdate();
			BeanUtils.copyProperties(responseEntity.getBody().getBody(), deviceResponsePayloadForAssetUpdate);
			return deviceResponsePayloadForAssetUpdate;
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}
	}

//		("/asset/update-asset-company")
	public Boolean updateAssetCompany(String logUUId, String accountNumber, String asstUuid) {
		Logutils.log("updateAssetCompany", logUUId, " Before calling [/asset/update-asset-company] from Device service",
				logger);

		ResponseEntity<ResponseDTO> responseEntity = deviceServiceFeignClient.updateAssetCompany(logUUId, accountNumber,
				asstUuid);
		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			Boolean status = responseEntity.getBody().getStatus();
			return status;
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}
	}

	// "/asset/asset-device-xref";
	public boolean saveAssetGatewayXref(SaveAssetGatewayXrefRequest saveAssetGatewayXrefRequest) {
		Logutils.log("saveAssetGatewayXref", saveAssetGatewayXrefRequest.getLogUUId(),
				" Before calling [/asset/asset-device-xref] from device service", logger);

		ResponseEntity<ResponseBodyDTO<AssetsPayload>> responseEntity = deviceServiceFeignClient
				.saveAssetGatewayXref(saveAssetGatewayXrefRequest);
		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			return true;
		} else {
			throw new InterServiceRestException(responseEntity.getBody().getMessage(), responseEntity.getStatusCode());
		}
	}

	// "/asset/is-asset-applicable-for-pre-pair/" + assetUuid;
	public boolean isAssetApplicableForPrePair(String logUUId, String assetUuid) {
		Logutils.log("isAssetApplicableForPrePair", logUUId,
				" Before calling [/asset/is-asset-applicable-for-pre-pair/] from device service", logger);

		ResponseEntity<ResponseDTO> responseEntity = deviceServiceFeignClient.isAssetApplicableForPrePair(logUUId,
				assetUuid);
		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			boolean body = responseEntity.getBody().getStatus();
			return true;
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}
	}

	// "/device/core/asset-sensor-xref/"
	public List<AssetSensorXref> getAllAssetSensorXrefForAssetUuid(String logUUId, String assetUuid) {
		Logutils.log("getAllAssetSensorXrefForAssetUuid", logUUId,
				" Before calling [/device/core/asset-sensor-xref/] from device service", logger);

		ResponseEntity<ResponseBodyDTO<List<AssetSensorXref>>> assetSensorXrefResponseEntity = deviceServiceFeignClient
				.getAllAssetSensorXrefForAssetUuid(logUUId, assetUuid);
		if (assetSensorXrefResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			List<AssetSensorXref> assetSensorList = new ArrayList<AssetSensorXref>();
			BeanUtils.copyProperties(assetSensorXrefResponseEntity.getBody().body, assetSensorList);
			return assetSensorXrefResponseEntity.getBody().body;
		} else {
			throw new InterServiceRestException("Http call not successful",
					assetSensorXrefResponseEntity.getStatusCode());
		}
	}

	// "/device/core/asset-sensor-xref";
	public boolean updateAssetSensorXref(String logUUId, List<AssetSensorXrefPayload> assetSensorXref) {
		Logutils.log("updateAssetSensorXref", logUUId,
				" Before calling [/device/core/asset-sensor-xref] from device service", logger);

		ResponseEntity<? extends Object> responseEntity = deviceServiceFeignClient
				.updateAssetSensorXref(assetSensorXref);
		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			return true;
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}
	}

//	     "/device/core/gateway-sensor-xref";
	public boolean saveGatewaySensorXref(String logUUId, List<DeviceSensorxrefPayload> deviceSensorxrefPayload) {
		Logutils.log("saveGatewaySensorXref", logUUId,
				" Before calling [/device/core/gateway-sensor-xref] from device service", logger);

		ResponseEntity<? extends Object> responseEntity = deviceServiceFeignClient
				.saveGatewaySensorXref(deviceSensorxrefPayload);
		Logutils.log("saveGatewaySensorXref", logUUId,
				" After calling [/device/core/gateway-sensor-xref] from device service", logger);

		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			return true;
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}
	}

//	    "/device/core/asset/can-vin?can=" + can + "&vin=" + vin;
	public Asset getAssetByVinAndCan(String logUUId, String vin, String can) {
		Logutils.log("getAssetByVinAndCan", logUUId, " Before calling [/device/core/asset/can-vin] from device service",
				logger);

		ResponseEntity<Asset> assetResponseEntity = deviceServiceFeignClient.getAssetByVinAndCan(logUUId, vin, can);
		Logutils.log(logUUId, " completed calling rest-api [/device/core/asset/can-vin] of Deivce ", logger);
		if (assetResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			return assetResponseEntity.getBody();
		} else {
			throw new InterServiceRestException("Http call not successful", assetResponseEntity.getStatusCode());
		}
	}

	public Device getGatewayByImeiAndCan(String logUUId, String imei, String can) {
		List<Device> deviceList = new ArrayList();
		ResponseEntity<ResponseBodyDTO<DeviceResponsePayload>> assetResponseEntity = deviceServiceFeignClient
				.getGatewayByImeiAndCan(logUUId, imei, can);

		if (assetResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			DeviceResponsePayload devicePayload = assetResponseEntity.getBody().body;
			Device device = new Device();
			BeanUtils.copyProperties(devicePayload, device);
			return device;
		} else {
			throw new InterServiceRestException("Http call not successful", assetResponseEntity.getStatusCode());
		}
	}

	public boolean resetInstallInDeviceService(String logUUId, Long assetId, Long gatewayId) {

		ResponseEntity<ResponseDTO> responseEntity = deviceServiceFeignClient.resetInstall(logUUId, assetId, gatewayId);
		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			boolean body = responseEntity.getBody().getStatus();
			return body;
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}
	}

//		/device/core/asset/
	public Asset getAssetByVin(String logUUid, String vin) {

		ResponseEntity<ResponseBodyDTO<Asset>> assetResponseEntity = deviceServiceFeignClient.getAssetByVin(logUUid,
				vin);
		if (assetResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			return assetResponseEntity.getBody().getBody();
		} else {
			throw new InterServiceRestException("Http call not successful", assetResponseEntity.getStatusCode());
		}
	}

	public ResponseEntity<Page<Device>> getGatewaysByCANAndStatusFromDeviceServiceWithPagination(String logUUid,
			String installCode, String accountNumber, Integer page, Integer pageSize, String sort, String order,
			List canList) {
		
		Logutils.log("updateSensor",  " Before calling [/device/gateway-pagination] from device service", logger);
		ResponseEntity<Page<Device>> result = deviceServiceFeignClient
				.getGatewaysByAccountNumberAndStatusWithPagination(logUUid, installCode, accountNumber, page, pageSize,
						sort, order, canList);

		Logutils.log("updateSensor",  " After calling [/device/gateway-pagination] from device service", logger);
		if (result.getStatusCode().equals(HttpStatus.OK)) {
			ParameterizedTypeReference<RestResponsePage<Device>> responseType = new ParameterizedTypeReference<RestResponsePage<Device>>() {
			};
			return result;
		} else {
			throw new InterServiceRestException("Http call not successful", result.getStatusCode());
		}

	}

	public List<Organisation> getCompanyByCustomer(String logUUid) {

		Logutils.log("getCompanyByCustomer",  " Before calling [/customer/core/customer/company] from device service", logger);
		ResponseEntity<List<Organisation>> companyResponseEntity = organisationServiceFeignClient
				.getAllCustomerCompany(logUUid);
		Logutils.log("getCompanyByCustomer",  " After calling [/customer/core/customer/company] from device service", logger);
		if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			List<Organisation> companies = companyResponseEntity.getBody();
			return companies;
		} else {
			throw new InterServiceRestException("Http call not successful", companyResponseEntity.getStatusCode());
		}
	}

	public Boolean deleteAssetByAssetUUID(String logUUid, String assetUuid) {

		ResponseEntity<? extends Object> assetResponseEntity = deviceServiceFeignClient.deleteAssetByAssetUuid(logUUid,
				assetUuid);
		if (assetResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			return true;
		} else {
			throw new InterServiceRestException("Http call not successful", assetResponseEntity.getStatusCode());
		}
	}

//		("/device/get-attribute-value")
	public List<AttributeResponseDTO> getAttributeValueDeviceId(String logUUid, String gateUuid) {

		ResponseEntity<List<AttributeResponseDTO>> attributeResponseEntity = deviceServiceFeignClient
				.getAllInstallationDetails(logUUid, gateUuid);

		if (attributeResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			List<AttributeResponseDTO> attributeResponseList = attributeResponseEntity.getBody();
			return attributeResponseList;
		} else {
			throw new InterServiceRestException("Http call not successful", attributeResponseEntity.getStatusCode());
		}
	}

	// Aamir
	public Device updateSensor(SensorUpdateRequest sensorUpdateRequest) {

		ResponseEntity<ResponseBodyDTO<DeviceResponsePayload>> responseEntity = deviceServiceFeignClient
				.updateSensor(sensorUpdateRequest);
		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			Device device = new Device();
			BeanUtils.copyProperties(responseEntity.getBody().body, device);
//	            Device body = (Device) responseEntity.getBody();
			return device;
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}
	}

//		("/device/status/gateway-asset")
	public Device updateGatewayAndAssetStatus(UpdateGatewayAssetStatusRequest updateGatewayAssetStatusRequest) {
		Logutils.log("updateGatewayAssetStatus", updateGatewayAssetStatusRequest.getLogUUId(),
				" Before calling [/device/status/gateway-asset] from Device service", logger);

		ResponseEntity<ResponseBodyDTO<DeviceResponsePayload>> responseEntity = deviceServiceFeignClient
				.updateGatewayAssetStatus(updateGatewayAssetStatusRequest);
		Logutils.log("updateGatewayAssetStatus", updateGatewayAssetStatusRequest.getLogUUId(),
				" After calling [/device/status/gateway-asset] from Device service", logger);

		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			DeviceResponsePayload body = responseEntity.getBody().body;
			Device d = new Device();
			BeanUtils.copyProperties(body, d);
			return d;
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}
	}

	// "/device/core/ms-device/assetToDevice";
	public Long updateAssetToDeviceInMS(String logUUId, InstallHistory installHistory) {
		Logutils.log("updateAssetToDeviceInMS", logUUId,
				" Before calling [device/core/ms-device/assetToDevice] from device service", logger);

		UpdateAssetToDeviceForInstallationRequest request = beanConverter
				.convertInstallHistoryToUpdateAssetToDeviceForInstallationRequest(installHistory);
		ResponseEntity<ResponseBodyDTO<Long>> response = deviceServiceFeignClient.updateAssetToDeviceInMS(request);
		if (response.getStatusCode().equals(HttpStatus.OK)) {
			return response.getBody().body;
		} else {
			throw new InterServiceRestException("Http call not successful", response.getStatusCode());
		}
	}

	public void sendFinishInstallToElastic(String logUUId, String deviceId, String assetId, String vin) {
		System.out.println("==========sendFinishInstallToElastic============ ");
		ElasticFinishInstallRequest request = new ElasticFinishInstallRequest();
		request.setAssetId(assetId);
		request.setDeviceId(deviceId);
		request.setVin(vin);
//	        HttpHeaders headers = new HttpHeaders();
//	        headers.add("Authorization", elasticFinishInstallToken);
//	        HttpEntity<ElasticFinishInstallRequest> entity = new HttpEntity(request, headers);
//	        try {
//	            restTemplate.postForLocation(elasticFinishInstallUrl, entity);
//	            logger.error("Updated elastic for installation with deviceId {}, assetId {}, vin {}", deviceId, assetId, vin);
//	        } catch(Exception e) {
//	            logger.error("Exception while updating installation in Elastic", e);
//	        }
	}
	// ---------------------------------------------Aamir
	// End------------------------------------------------------//

	// --------------------------------Aamir Start 1
	// ---------------------------------------//

	public Organisation getOrganisationByUuidFromCompanyService(String companyUuid) {
		Logutils.log("getOrganisationByUuidFromCompanyService",
				" Before calling [/customer/core/uuid] from organisation service", logger);
		ResponseEntity<Organisation> responseEntity = organisationServiceFeignClient.getCompanyForUuid(companyUuid);
		Logutils.log("getOrganisationByUuidFromCompanyService",
				" After calling [/customer/core/uuid] from organisation service", logger);
		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			Organisation organisation = responseEntity.getBody();
			return organisation;
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}
	}

	public List<DeviceResponsePayload> getSensorByCan(String can) {
		ResponseEntity<ResponseBodyDTO<List<DeviceResponsePayload>>> responseEntity1 = deviceServiceFeignClient
				.getSensorForCan(can);
//	    	List<Device> device = new ArrayList<>(); 

		if (responseEntity1.getStatusCode().equals(HttpStatus.OK)) {
//	        	List<Device> device1 = new ArrayList<>();
//	    		
//	    				for (DeviceResponsePayload element : responseEntity.getBody().body){
//	    					Device device2 = new Device();
//	    					BeanUtils.copyProperties(element, device2);
//	    					device.add(device2);
//	    				}	
//		     List<DeviceResponsePayload> body = responseEntity.getBody().getBody();
			return responseEntity1.getBody().body;
		} else {
			throw new InterServiceRestException("Http call not successful", HttpStatus.NOT_FOUND);
		}

	}

	public Device getGatewayByUuid(String uuid) {
		Logutils.log("getGatewayByUuid", " Before calling [/device/gateway/uuid/] from device service", logger);
		ResponseEntity<ResponseBodyDTO<Device>> deviceResponseEntity = deviceServiceFeignClient.getGatewayByUuid(uuid);
		Logutils.log("getGatewayByUuid", " After calling [/device/gateway/uuid/] from device service", logger);
		if (deviceResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			return deviceResponseEntity.getBody().getBody();
		} else {
			throw new InterServiceRestException("Http call not successful", deviceResponseEntity.getStatusCode());
		}
	}

	public List<Device> getGatewaysByCANAndStatusFromDeviceService(String logUUid, String accountNumber,
			String status) {
		ResponseEntity<ResponseBodyDTO<List<Device>>> responseEntity = null;
		Logutils.log(logUUid, " Before calling [/device/can-status] from device service", logger);
		if (status != null && !status.isEmpty()) {
			responseEntity = deviceServiceFeignClient.getGatewaysByAccountNumberAndStatus(logUUid, accountNumber,
					status);
		} else {
			Logutils.log(logUUid, " Before calling [/device/can-status] from device service", logger);
			responseEntity = deviceServiceFeignClient.getGatewaysByAccountNumberAndStatus(logUUid, accountNumber, null);
		}
		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			return responseEntity.getBody().getBody();
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}

	}

	public List<Organisation> getCompanyByCustomer() {
//      Application application = eurekaClient.getApplication(gatewayServiceId);
//      InstanceInfo instanceInfo = application.getInstances().get(0);
//      String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/customer/core/company";
//        ResponseEntity<Company[]> companyResponseEntity = restTemplate.getForEntity(url, Company[].class);
		Logutils.log("getCompanyByCustomer", "Before calling [/customer/organisation] from organisation service", logger);
		ResponseEntity<ResponseBodyDTO<List<Organisation>>> organisationResponseEntity = organisationServiceFeignClient
				.getAllCustomerCompany();
		Logutils.log("getCompanyByCustomer", "After calling [/customer/organisation] from organisation service", logger);

		if (organisationResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			List<Organisation> organisation = Arrays.asList(organisationResponseEntity.getBody().getBody().get(0));
			return organisation;
		} else {
			throw new InterServiceRestException("Http call not successful", organisationResponseEntity.getStatusCode());
		}
	}

	public List<Attribute> getAttributeListByProductName(String logUUid, String productName, String gateUuid) {
		Logutils.log(logUUid, " Before calling [/device/product-name/{productName}] from device service", logger);

		ResponseEntity<ResponseBodyDTO<List<Attribute>>> attributeResponseEntity = deviceServiceFeignClient
				.getAttributeListByProductName(productName);
		Logutils.log(logUUid, " After calling [/device/product-name/{productName}] from device service", logger);

		if (attributeResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			List<Attribute> attributeResponseList = attributeResponseEntity.getBody().getBody();
			return attributeResponseList;
		} else {
			throw new InterServiceRestException("Http call not successful", attributeResponseEntity.getStatusCode());
		}
	}

	public String getLookupValueFromDeviceService(String field) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/lookup?field=" + field;  	
//    	ResponseEntity<String> lookupResponseEntity = restTemplate.getForEntity(url, String.class);
		Logutils.log("getLookupValueFromDeviceService "," Before calling [/device/lookup] from device service", logger);
		ResponseEntity<String> lookupResponseEntity = deviceServiceFeignClient.getLookupValue(field);
		Logutils.log("getLookupValueFromDeviceService "," After calling [/device/lookup] from device service", logger);
		if (lookupResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			return lookupResponseEntity.getBody();
		} else {
			throw new InterServiceRestException("Http call not successful", lookupResponseEntity.getStatusCode());
		}
	}

	public User getUserFromAuthServiceByName(String userName) {
		// Logutils.log("getUserFromAuthService", logUUId, " Before calling [/user/get/]
		// from Auth service", logger);
		 Logutils.log("getUserFromAuthService",  " Before calling [/user/get/] from Auth service", logger);
         ResponseEntity<UserDTO> userResponseEntity = authServiceClient.getUserFromAuthServiceByName(userName);
         Logutils.log("getUserFromAuthService",  " After calling [/user/get/] from Auth service", logger);
		if (userResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			User user = objectMapper.convertValue(userResponseEntity.getBody(), User.class);
			return user;
		} else {
			throw new InterServiceRestException("Http call not successful", userResponseEntity.getStatusCode());
		}
	}

	// --------------------------------Aamir End 1
	// ---------------------------------------//

	// "/customer/core/" + accountNumber;
	public Organisation getCompanyFromCompanyService(String logUUId, String accountNumber) {
		Logutils.log("getCompanyFromCompanyService", logUUId,
				" Before calling [/customer/core] from organition service", logger);

		ResponseEntity<Organisation> responseEntity = organisationServiceFeignClient.getCompanyForAccountNumber(logUUId,
				accountNumber);
		Logutils.log("getCompanyFromCompanyService", logUUId,
				"After calling [/customer/core] from organition service", logger);

		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			Organisation organisation = responseEntity.getBody();
			return organisation;
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}
	}

	public List<AssetDTO> getAssetsByCANAndStatusFromDeviceServiceUsingDTO(String logUUId, String accountNumber,
			String status) {

		ResponseEntity<ResponseBodyDTO<List<AssetDTO>>> assetsResponseEntity = deviceServiceFeignClient
				.getAssetsByAccountNumberAndStatusUsingDTO(logUUId, accountNumber, status);

		if (assetsResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			List<AssetDTO> assetList = assetsResponseEntity.getBody().getBody();
			return assetList;
		} else {
			throw new InterServiceRestException("Http call not successful", assetsResponseEntity.getStatusCode());
		}
	}

//	("/device")
	public List<Device> getSensorBySensorUuid(String logUUid, String sensorUuid) {
		Logutils.log("getDevice", logUUid, " Before calling [/device] from device service", logger);
        ResponseEntity<ResponseBodyDTO<List<DeviceResponsePayload>>> responseEntity = deviceServiceFeignClient
				.getDevice(sensorUuid);
		Logutils.log("getDevice", logUUid, " After calling [/device] from device service", logger);
        List<Device> deviceList = new ArrayList<>();
		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			List<DeviceResponsePayload> devicePayload = responseEntity.getBody().getBody();
			Device device = new Device();
			for (DeviceResponsePayload deviceResponsePayload : devicePayload) {
				BeanUtils.copyProperties(deviceResponsePayload, device);
				deviceList.add(device);
				System.out.println("Device is" + device);
			}
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}
		return deviceList;
	}

//	("/device/gateway/status")
	public Device updateGatewayStatusAndDate(InstallationStatusGatewayRequest installationStatusGatewayRequest) {
		Logutils.log("updateGatewayStatus", "", " Before calling [/device/gateway/status] from device service", logger);
		Device device = new Device();
		ResponseEntity<ResponseBodyDTO<DeviceResponsePayload>> responseEntity = deviceServiceFeignClient
				.updateGatewayStatus(installationStatusGatewayRequest);
		Logutils.log("updateGatewayStatus", "", " After calling [/device/gateway/status] from device service", logger);

		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			BeanUtils.copyProperties(responseEntity.getBody().getBody(), device);
			return device;
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}
	}

	// get Asset Details for Maint reports
	public AssetsStatusPayload getAssetsDetails(String imei) {
		List<AssetsStatusPayload> assetPayloadDetails = new ArrayList<>();
		AssetsStatusPayload assetPayload = new AssetsStatusPayload();
		Logutils.log("getAssetsDetails", "", " Before calling [/gateway/assetDetails] from device service", logger);
		ResponseEntity<ResponseBodyDTO<List<AssetsStatusPayload>>> responseEntity = deviceServiceFeignClient
				.getAssetsDetails(imei);
		Logutils.log("getAssetsDetails", "", " After calling [/gateway/assetDetails] from device service", logger);
		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			assetPayloadDetails = responseEntity.getBody().getBody();
			assetPayload = assetPayloadDetails.get(0);
			return assetPayload;
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}
	}

//    public Gateway getGatewayByUuid(String uuid) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/gateway/uuid/" + uuid;
//        ResponseEntity<Gateway> gatewayResponseEntity = restTemplate.getForEntity(url, Gateway.class);
//        if (gatewayResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            return gatewayResponseEntity.getBody();
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", gatewayResponseEntity.getStatusCode());
//        }
//    }
//    public ArrayList<Sensor> getAllSensorsForGatewayUuid(String gatewayUuid) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/sensor/gateway/" + gatewayUuid;
//        ResponseEntity<ArrayList> gatewayResponseEntity = restTemplate.getForEntity(url, ArrayList.class);
//        if (gatewayResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            return gatewayResponseEntity.getBody();
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", gatewayResponseEntity.getStatusCode());
//        }
//    }
//
//   

	// Aamir
//    public List<SubSensor> findBySensorUuid(String subSensorUuid) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/sub-sensor/" + subSensorUuid;
//        ResponseEntity<SubSensor[]> subSensorsResponseEntity = restTemplate.getForEntity(url, SubSensor[].class);
//        if (subSensorsResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            List<SubSensor> subSensorsResponseEntityResponseList = Arrays.asList(subSensorsResponseEntity.getBody());
//            return subSensorsResponseEntityResponseList;
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", subSensorsResponseEntity.getStatusCode());
//        }
//    }

//    public InventoryResponse getInventoryForCustomer(String accountNumber) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/inventory/" + accountNumber;
//        ResponseEntity<ResponseBodyDTO> inventoryResponseResponseEntity = restTemplate.getForEntity(url, ResponseBodyDTO.class);
//        if (inventoryResponseResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            ResponseBodyDTO<InventoryResponse> body = inventoryResponseResponseEntity.getBody();
//            InventoryResponse inventoryResponseFromMap = beanConverter.createInventoryResponseFromMap((Map) body.getBody());
//            return inventoryResponseFromMap;
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", inventoryResponseResponseEntity.getStatusCode());
//        }
//    }
//
//    public Sensor getSensorBySensorUuid(String sensorUuid) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/sensor/uuid/" + sensorUuid;
//        ResponseEntity<Object> sensorResponseEntity = restTemplate.getForEntity(url, Object.class);
//        if (sensorResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            Sensor body = objectMapper.convertValue(((Map<String, Map>) sensorResponseEntity.getBody()).get("body"), Sensor.class);
//            return body;
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", sensorResponseEntity.getStatusCode());
//        }
//    }

	public Device updateSensor(Device sensor) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/update-sensor";
//        HttpEntity<Sensor> httpEntity = new HttpEntity<>(sensor);
//        ResponseEntity<Object> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, Object.class);

		Logutils.log("updateSensor",  " Before calling [/device/update-sensor] from device service", logger);
		ResponseEntity<ResponseBodyDTO<Device>> responseEntity = deviceServiceFeignClient
				.updateSensorBySensorObj(sensor);
		Logutils.log("updateSensor",  " After calling [/device/update-sensor] from device service", logger);
		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			Device body = responseEntity.getBody().body;
			return body;
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}
	}

//    public Gateway updateGatewayStatusAndDate(InstallationStatusGatewayRequest installationStatusGatewayRequest) {
////        Application application = eurekaClient.getApplication(gatewayServiceId);
////        InstanceInfo instanceInfo = application.getInstances().get(0);
////        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/gateway/status";
//       // HttpEntity entity = new HttpEntity(installationStatusGatewayRequest);
//       // ResponseEntity<Object> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, entity, Object.class);
//        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            Gateway body = objectMapper.convertValue(((Map<String, Map>) responseEntity.getBody()).get("body"), Gateway.class);
//            return body;
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", responseEntity.getStatusCode());
//        }
//    }
//    public Gateway resetGateway(InstallationStatusGatewayRequest installationStatusGatewayRequest) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/gateway/reset";
//        HttpEntity entity = new HttpEntity(installationStatusGatewayRequest);
//        ResponseEntity<Object> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, entity, Object.class);
//        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            Gateway body = objectMapper.convertValue(((Map<String, Map>) responseEntity.getBody()).get("body"), Gateway.class);
//            return body;
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", responseEntity.getStatusCode());
//        }
//    }
//   
//"/device/core/status/gateway-asset";
	public Device updateDeviceAndAssetStatus(String logUUId,
			UpdateGatewayAssetStatusRequest updateDeviceAssetStatusRequest) {
//		logger.info("Update asset and gateway request for finish installation");
//		logger.info("request parameter of gateway  for finish install  " + updateDeviceAssetStatusRequest.getGatewayUuid() + " " + updateDeviceAssetStatusRequest.getAssetUuid());
//		logger.info("request parameter of asset  for finish install  " + updateDeviceAssetStatusRequest.getAssetUuid() + " " + updateDeviceAssetStatusRequest.getAssetStatus());

		Logutils.log("updateDeviceAssetStatus", logUUId,
				" Before calling [/status/device-asset] from device service", logger);
		Logutils.log("updateDeviceAssetStatus", logUUId,
				" After calling [/status/device-asset] from device service", logger);
		ResponseEntity<Object> responseEntity = deviceServiceFeignClient
				.updateDeviceAssetStatus(updateDeviceAssetStatusRequest);
		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {

			return (Device) responseEntity.getBody();
		} else {
			throw new InterServiceRestException("Http call not successful", responseEntity.getStatusCode());
		}
	}

//    public List<Asset> getAssetsByCANAndStatusFromDeviceService(String accountNumber, String status) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url;
//        if (status != null && !status.isEmpty()) {
//            url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/asset?can=" + accountNumber + "&status=" + status;
//        } else {
//            url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/asset?can=" + accountNumber;
//        }
//        ResponseEntity assetsResponseEntity = restTemplate.getForEntity(url, Object.class);
//        if (assetsResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            List<Asset> assetList = beanConverter.createListOfAssetsFromMap((Map) assetsResponseEntity.getBody());
//            return assetList;
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", assetsResponseEntity.getStatusCode());
//        }
//    }
//    
//    public List<AssetDTO> getAssetsByCANAndStatusFromDeviceServiceUsingDTO(String accountNumber, String status) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url;
//        if (status != null && !status.isEmpty()) {
//            url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/asset?can=" + accountNumber + "&status=" + status;
//        } else {
//            url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/asset?can=" + accountNumber;
//        }
//        ResponseEntity assetsResponseEntity = restTemplate.getForEntity(url, Object.class);
//        if (assetsResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            List<AssetDTO> assetList = beanConverter.createListOfAssetDTOsFromMap((Map) assetsResponseEntity.getBody());
//            return assetList;
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", assetsResponseEntity.getStatusCode());
//        }
//    }
//
//    public List<Gateway> getGatewaysByCANAndStatusFromDeviceService(String accountNumber, String status) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url;
//        if (status != null && !status.isEmpty()) {
//            url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/gateway?can=" + accountNumber + "&status=" + status;
//        } else {
//            url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/gateway?can=" + accountNumber;
//        }
//        ResponseEntity gatewayResponseEntity = restTemplate.getForEntity(url, Object.class);
//        if (gatewayResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            List<Gateway> gatewayList = beanConverter.createListOfGatewaysFromMap((Map) gatewayResponseEntity.getBody());
//            return gatewayList;
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", gatewayResponseEntity.getStatusCode());
//        }
//    }
//    

//
//    public Company getCompanyFromCompanyService(String accountNumber) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/customer/core/" + accountNumber;
//        ResponseEntity<Company> companyResponseEntity = restTemplate.getForEntity(url, Company.class);
//        if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            Company company = companyResponseEntity.getBody();
//            return company;
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", companyResponseEntity.getStatusCode());
//        }
//    }
//
	public List<Organisation> getCompanyByTypeFromCompanyService(String logUUid, String type) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/customer/core/type/" + type;
//        ResponseEntity<Organisation[]> companyResponseEntity = restTemplate.getForEntity(url, Organisation[].class);

		ResponseEntity<List<Organisation>> companyResponseEntity = organisationServiceFeignClient
				.getListOfOrganisationForType(logUUid, type);
		if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			List<Organisation> companies = Arrays.asList(companyResponseEntity.getBody().get(0));
			return companies;
		} else {
			throw new InterServiceRestException("Http call not successful", companyResponseEntity.getStatusCode());
		}
	}

	public List<Organisation> getCustomerCompany(String logUUId) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/customer/core/customer/company";
//        ResponseEntity<Organisation[]> companyResponseEntity = restTemplate.getForEntity(url, Company[].class);
		ResponseEntity<List<Organisation>> companyResponseEntity = organisationServiceFeignClient
				.getAllCustomerCompany(logUUId);
		if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			List<Organisation> companies = companyResponseEntity.getBody();
			return companies;
		} else {
			throw new InterServiceRestException("Http call not successful", companyResponseEntity.getStatusCode());
		}
	}

	public List<OrganisationDtoForInventory> getCustomerCompanyDto(String logUUId) {
		ResponseEntity<List<OrganisationDtoForInventory>> companyResponseEntity = organisationServiceFeignClient
				.getAllCustomerCompanyDto(logUUId);
		if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
			List<OrganisationDtoForInventory> companies = companyResponseEntity.getBody();
			return companies;
		} else {
			throw new InterServiceRestException("Http call not successful", companyResponseEntity.getStatusCode());
		}
	}

	public ResponseEntity<Page<Device>> getGatewaysByCANAndStatusFromDeviceServiceWithPaginationNew(
			String accountNumber, Integer page, Integer pageSize, String sort, String order, List canList,
			String timeOfLastDownload) {
//		Application application = eurekaClient.getApplication(gatewayServiceId);
//		InstanceInfo instanceInfo = application.getInstances().get(0);
//		String url;
//		if (timeOfLastDownload != null && !timeOfLastDownload.isEmpty() && timeOfLastDownload != "" && accountNumber != null && !accountNumber.isEmpty()) {
//			url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/gateway-pagination-new?_page=" + page
//					+ "&_limit=" + pageSize + "&_sort=" + sort + "&_order=" + order + "&can=" + accountNumber + "&time_of_last_download=" + timeOfLastDownload;
//		} else if (accountNumber != null && !accountNumber.isEmpty()) {
//			url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/gateway-pagination-new?_page=" + page
//					+ "&_limit=" + pageSize + "&_sort=" + sort + "&_order=" + order + "&can=" + accountNumber;
//		} else if(timeOfLastDownload != null && !timeOfLastDownload.isEmpty() && timeOfLastDownload != "") {
//			url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/gateway-pagination-new?_page=" + page
//					+ "&_limit=" + pageSize + "&_sort=" + sort + "&_order=" + order + "&cans=" + canList + "&time_of_last_download=" + timeOfLastDownload;
//		} else {
//			url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/gateway-pagination-new?_page=" + page
//					+ "&_limit=" + pageSize + "&_sort=" + sort + "&_order=" + order + "&cans=" + canList;
//		}
//
//		ParameterizedTypeReference<RestResponsePage<Device>> responseType = new ParameterizedTypeReference<RestResponsePage<Device>>() {
//		};
//		ResponseEntity<RestResponsePage<Device>> result = restTemplate.exchange(url, HttpMethod.GET, null/*httpEntity*/, responseType);
		ResponseEntity<Page<Device>> result = deviceServiceFeignClient
				.getGatewaysByAccountNumberAndStatusWithPaginationNew(accountNumber, "", page, pageSize, sort, order,
						canList, timeOfLastDownload);

		if (result.getStatusCode().equals(HttpStatus.OK)) {
			return result;
		} else {
			throw new InterServiceRestException("Http call not successful", result.getStatusCode());
		}

	}

//    public List<Sensor> getSensorByCan(String can) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/sensor/can?can=" + can;
//        ResponseEntity<ArrayList> responseEntity = restTemplate.getForEntity(url, ArrayList.class);
//        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            List<Sensor> listOfSensorsFromMap = beanConverter.createListOfSensorsFromMap((List<Map>) responseEntity.getBody());
//            return listOfSensorsFromMap;
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", responseEntity.getStatusCode());
//        }
//    }
//
//    public Company getCompanyByUuidFromCompanyService(String companyUuid) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/customer/core/uuid/" + companyUuid;
//        ResponseEntity<Company> companyResponseEntity = restTemplate.getForEntity(url, Company.class);
//        if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            Company company = companyResponseEntity.getBody();
//            return company;
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", companyResponseEntity.getStatusCode());
//        }
//    }
//

//    public void sendFinishInstallToElastic(String logUUId, String deviceId, String assetId, String vin) {
//    	System.out.println("==========sendFinishInstallToElastic============ ");  
//    	ElasticFinishInstallRequest request = new ElasticFinishInstallRequest();
//        request.setAssetId(assetId);
//        request.setDeviceId(deviceId);
//        request.setVin(vin);
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization", elasticFinishInstallToken);
//        HttpEntity<ElasticFinishInstallRequest> entity = new HttpEntity(request, headers);
//        try {
//            restTemplate.postForLocation(elasticFinishInstallUrl, entity);
//            logger.error("Updated elastic for installation with deviceId {}, assetId {}, vin {}", deviceId, assetId, vin);
//        } catch(Exception e) {
//            logger.error("Exception while updating installation in Elastic", e);
//        }
//    }

//    public List<AttributeValue> getAttributeValueDeviceId(String gateUuid) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/device/core/get-attribute-value?deviceId="+gateUuid;
//        ResponseEntity<AttributeValue[]> attributeResponseEntity = restTemplate.getForEntity(url, AttributeValue[].class);
//        if (attributeResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            List<AttributeValue> attributeResponseList = Arrays.asList(attributeResponseEntity.getBody());
//            return attributeResponseList;
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", attributeResponseEntity.getStatusCode());
//        }
//    }
//    
//
// 
//
//    public List<AttributeResponse> getAttributeListByProductCode(String productCode,String gatewayUuid) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization", elasticFinishInstallToken);
//        HttpEntity<String> request = new HttpEntity<String>(headers);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/product/attribute-list/product-code?product-code=" + productCode + "&gateway-uuid=" + gatewayUuid;
//        ResponseEntity<AttributeResponse[]> attributeResponseEntity = restTemplate.exchange(url, HttpMethod.GET, request, AttributeResponse[].class);
//        if (attributeResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            List<AttributeResponse> attributeResponseList = Arrays.asList(attributeResponseEntity.getBody());
//            return attributeResponseList;
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", attributeResponseEntity.getStatusCode());
//        }
//    }
//    
//    public boolean checkAutoResetValueForCompany(Long companyId) {
//        Application application = eurekaClient.getApplication(gatewayServiceId);
//        InstanceInfo instanceInfo = application.getInstances().get(0);
//        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/customer/core/checkAutoResetValue/" + companyId;
//        ResponseEntity<ResponseDTO> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, ResponseDTO.class);
//        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
//            boolean body = responseEntity.getBody().getStatus();
//            return body;
//        } else {
//            throw new InterServiceRestException
//                    ("Http call not successful", responseEntity.getStatusCode());
//        }
//    }
	// "/asset/update-asset-company?account_number=" +
	// accountNumber+"&asset_uuid="+asstUuid;

	/*
	 * public boolean callSendUDPCommandAPI(String deviceId) {
	 * logger.info("Inside the method : callSendUDPCommandAPI from Rest Util Call");
	 * String url = sendUDPCommandURL + "?device_id=" + deviceId;
	 * logger.info("API URL : " + url); Boolean flag = false; try { //
	 * List<HttpMessageConverter<?>> messageConverters = new
	 * ArrayList<HttpMessageConverter<?>>(); // MappingJackson2HttpMessageConverter
	 * converter = new MappingJackson2HttpMessageConverter(); //
	 * converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
	 * // messageConverters.add(converter); //
	 * restTemplate.setMessageConverters(messageConverters);
	 * ResponseEntity<ResponseDTOForUDPCommand> responseEntity =
	 * restTemplate.exchange(url, HttpMethod.GET, null,
	 * ResponseDTOForUDPCommand.class); if
	 * (responseEntity.getStatusCode().equals(HttpStatus.OK)) { flag = true;
	 * logger.info("If status OK for the method : callSendUDPCommandAPI and flag : "
	 * + flag); logger.info("Response Object ====> "); logger.info(responseEntity !=
	 * null && responseEntity.getBody() != null ?
	 * responseEntity.getBody().getStatus() : ""); } else { logger.
	 * info("If not status OK for the method : callSendUDPCommandAPI and flag : " +
	 * flag); logger.info("Response Object ====> "); logger.info(responseEntity !=
	 * null && responseEntity.getBody() != null ?
	 * responseEntity.getBody().getStatus() : ""); } } catch (Exception e) { logger.
	 * error("Exception occured while calling method : callSendUDPCommandAPI and Error is : "
	 * + e.getMessage()); } logger.
	 * info("Exiting from the method : callSendUDPCommandAPI from Rest Util Call and final flag is : "
	 * + flag); return flag; }
	 */

	public ResponseEntity<Page<GatewayDetailsBean>> getGatewaysByCANAndStatusFromDeviceServiceWithPaginationV2(
			String accountNumber, Integer page, Integer pageSize, String sort, String order, List canList,
			String timeOfLastDownload) {
		Logutils.log("getGatewaysByCANAndStatusFromDeviceServiceWithPaginationV2", " Before calling [/device/gateway-pagination-v2] from device service", logger);

		ResponseEntity<Page<GatewayDetailsBean>> result = deviceServiceFeignClient
				.getGatewaysByAccountNumberAndStatusWithPaginationV2(accountNumber, "", page, pageSize, sort, order,
						canList, timeOfLastDownload);
		Logutils.log("getGatewaysByCANAndStatusFromDeviceServiceWithPaginationV2", " After calling [/device/gateway-pagination-v2] from device service", logger);
		if (result.getStatusCode().equals(HttpStatus.OK)) {
			return result;
		} else {
			throw new InterServiceRestException("Http call not successful", result.getStatusCode());
		}

	}

}
