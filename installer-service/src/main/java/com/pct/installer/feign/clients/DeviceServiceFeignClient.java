package com.pct.installer.feign.clients;

import java.util.List;

import javax.validation.Valid;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.pct.common.dto.AssetDTO;
import com.pct.common.dto.DeviceResponsePayloadForAssetUpdate;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.model.Asset;
//import com.pct.common.model.AssetGatewayXref;
import com.pct.common.model.AssetSensorXref;
import com.pct.common.model.Attribute;
import com.pct.common.model.AttributeValue;
import com.pct.common.model.Device;
import com.pct.common.payload.AssetSensorXrefPayload;
import com.pct.common.payload.AssetsPayload;
import com.pct.common.payload.DeviceSensorxrefPayload;
import com.pct.common.payload.GatewayDetailsBean;
import com.pct.common.payload.InstallationStatusGatewayRequest;
import com.pct.common.payload.SaveAssetGatewayXrefRequest;
import com.pct.common.payload.SensorUpdateRequest;
import com.pct.common.payload.UpdateAssetToDeviceForInstallationRequest;
//import com.pct.common.payload.UpdateDeviceAssetStatusRequest;
import com.pct.common.payload.UpdateGatewayAssetStatusRequest;
import com.pct.installer.dto.AttributeResponseDTO;
import com.pct.installer.feign.config.DeviceServiceFeignConfig;
import com.pct.installer.payload.AssetsStatusPayload;
import com.pct.installer.payload.DeviceResponsePayload;

import feign.Headers;
import feign.Param;


@FeignClient(name="device-service", configuration = DeviceServiceFeignConfig.class)
//@Headers("logUUId_Header: {logUUId}")  
public interface DeviceServiceFeignClient {
	
	@GetMapping(path = "/device")
//	@RequestMapping(method = RequestMethod.GET, value = "/device/imei/{imei}", produces = "application/json")
	public ResponseEntity<ResponseBodyDTO<List<DeviceResponsePayload>>> getGatewayByImei(@RequestParam(value = "logUUId", required = false) String logUUId, 
			 																			  @RequestParam(value = "device-id", required = false) String imei);
	
//	 @GetMapping("/device/imei/{imei}")
//	 ResponseEntity<ResponseBodyDTO<DeviceResponsePayload>> getDeviceByImei(@RequestParam(value = "logUUId", required = false) String logUUId, @PathVariable("imei") String imei);
	
//	 public String getGatewayByImei(@PathVariable("imei") String imei);
	 // gatewayUuid  assetUuid set in assetDevicePayload;      String gatewayUuid, String assetUuid
	@PostMapping("/device/update-asset" )
	public ResponseEntity<ResponseBodyDTO<List<DeviceResponsePayload>>> updateAssetForGateway(@RequestParam(value = "logUUId", required = false) String logUUId ,
																					    @RequestParam(value = "gatewayUuid", required = true) String gatewayUuid,
																					    @RequestParam(value = "assetUuid" , required = true) String assetUuid);
	
	@PostMapping("/device/update-asset-v1" )
	public ResponseEntity<ResponseBodyDTO<DeviceResponsePayloadForAssetUpdate>> updateAssetForGatewayV1(@RequestParam(value = "logUUId", required = false) String logUUId ,
																					    @RequestParam(value = "gatewayUuid", required = true) String gatewayUuid,
																					    @RequestParam(value = "assetUuid" , required = true) String assetUuid);
	
	
	@GetMapping("/device/can-mac")
	public  ResponseEntity<ResponseBodyDTO<List<DeviceResponsePayload>>> getGatewayByMACAndCan( @RequestParam(value = "logUUId", required = false) String logUUId ,
																								  @RequestParam(value = "mac", required = true) String mac,
																					              @RequestParam(value = "can", required = false) String can);
	
	@GetMapping("/asset/uuid/{uuid}")
	public ResponseEntity<ResponseBodyDTO<List<AssetsPayload>>>  getAssetByAssetUUID(@RequestParam(value = "logUUId", required = false) String logUUId ,
																			   @PathVariable("uuid") String uuid); 
	
	@PostMapping("/asset/asset-device-xref")
	public ResponseEntity<ResponseBodyDTO<AssetsPayload>>  saveAssetGatewayXref(@RequestBody SaveAssetGatewayXrefRequest saveAssetGatewayXrefRequest);
	
	@PostMapping("/status/device-asset")
	public ResponseEntity<Object> updateDeviceAssetStatus(@Valid @RequestBody UpdateGatewayAssetStatusRequest updateDeviceAssetStatusRequest);
	
	@PutMapping("/device/ms-device/assetToDevice")
	public ResponseEntity<ResponseBodyDTO<Long>> updateAssetToDeviceInMS(@Valid @RequestBody UpdateAssetToDeviceForInstallationRequest request);
	    
	@GetMapping("/asset/is-asset-applicable-for-pre-pair/{assetUuid}")
	public ResponseEntity<ResponseDTO> isAssetApplicableForPrePair(@RequestParam(value = "logUUId", required = false) String logUUId , 
			 														@PathVariable("assetUuid") String assetUuid) ;
	 
	@PostMapping("/asset/update-asset-company")
	public ResponseEntity<ResponseDTO> updateAssetCompany(@RequestParam(value = "logUUId", required = false) String logUUId , 
														  @RequestParam(value = "account_number", required = true) String accountNumber,
														  @RequestParam(value = "asset_uuid", required = true) String asset_uuid);
	
	@GetMapping("/asset/asset-sensor-xref/{assetUuid}")
	ResponseEntity<ResponseBodyDTO<List<AssetSensorXref>>>  getAllAssetSensorXrefForAssetUuid(@RequestParam(value = "logUUId", required = false) String logUUId ,
																							  @PathVariable("assetUuid") String assetUuid);

	@PutMapping("/device/asset-sensor-xref")
	ResponseEntity<? extends Object> updateAssetSensorXref(@Valid @RequestBody List<AssetSensorXrefPayload> assetSensorXref);	
	
	@GetMapping("/asset/can-vin")
	public ResponseEntity<Asset>  getAssetByVinAndCan(@RequestParam(value = "logUUId", required = false) String logUUId, 
													  @RequestParam(value = "vin", required = true) String vin,
										              @RequestParam(value = "can", required = false) String can);
	
//	@GetMapping("/asset/imei-vin")
//	public ResponseEntity<Device>  getGatewayByImeiAndCan(@Param("logUUId") String logUUId, @RequestParam(value = "imei", required = true) String imei,
//            @RequestParam(value = "can", required = false) String can);
//	
    @PutMapping("/device/reset-install")
    public ResponseEntity<ResponseDTO> resetInstall(@RequestParam(value = "logUUId", required = false) String logUUId, 
    												@RequestParam(name = "assetId", required = false) Long assetId,
                                                    @RequestParam(name = "gatewayId", required = false) Long gatewayId);
	
//	@PutMapping("/device/sensor")
//	public ResponseEntity<? extends Object> updateSensor(@Param("logUUId") String logUUId, @Valid @RequestBody SensorUpdateRequest sensorUpdateRequest);
	
	@PutMapping("/device/gateway-sensor-xref")
	public ResponseEntity<? extends Object> saveGatewaySensorXref(@Valid @RequestBody List<DeviceSensorxrefPayload> deviceDevicexref);
	
	@PutMapping("/device/gateway/status")
	public ResponseEntity<ResponseBodyDTO<DeviceResponsePayload>> updateGatewayStatus(@Valid @RequestBody InstallationStatusGatewayRequest installationStatusGatewayRequest);
	
	@GetMapping("/device")
	public ResponseEntity<ResponseBodyDTO<List<DeviceResponsePayload>>> getDevice(@RequestParam(value = "device-uuid", required = false) String uuid);
	
	@PutMapping("/device/status/gateway-asset")
	 public ResponseEntity<ResponseBodyDTO<DeviceResponsePayload>> updateGatewayAssetStatus(@RequestBody UpdateGatewayAssetStatusRequest updateGatewayAssetStatusRequest);
		//TODO: process PUT request
	
	//Aamir
    @GetMapping("/device/can-imei")
    public ResponseEntity<ResponseBodyDTO<DeviceResponsePayload>>  getGatewayByImeiAndCan(@RequestParam(value = "logUUId", required = false) String logUUId,
    																							@RequestParam(value = "imei", required = true) String imei,
    																							@RequestParam(value = "can", required = true) String can);
    
    @GetMapping("/device/asset")
    public ResponseEntity<ResponseBodyDTO<List<AssetDTO>>> getAssetsByAccountNumberAndStatusUsingDTO(@RequestParam(value = "logUUid", required = false) String logUUid, 
																		    		  @RequestParam(value = "can") String accountNumber, 
																		    		  @RequestParam(value = "status", required = false) String status);
    
    @GetMapping("/asset/uuid/delete/{assetUuid}")
    public ResponseEntity<? extends Object> deleteAssetByAssetUuid(@RequestParam(value = "logUUid", required = false) String logUUid, @PathVariable("assetUuid") String assetUuid);
    
    @GetMapping("/asset/vin/{vin}")
    public ResponseEntity<ResponseBodyDTO<Asset>> getAssetByVin(@RequestParam(value = "logUUid", required = false) String logUUid, 
    													  @PathVariable("vin") String vin);
    
    @GetMapping("/device/get-attribute-value")
   	public ResponseEntity<List<AttributeResponseDTO>> getAllInstallationDetails(@RequestParam(value = "logUUid", required = false) String logUUid,
   																		 @RequestParam(value = "deviceId", required = true) String deviceId);
  
    //---------------------------------Aamir 1 Start----------------------------------------//

    @GetMapping("/device/sensor/can")
	public ResponseEntity<ResponseBodyDTO<List<DeviceResponsePayload>>> getSensorForCan(@RequestParam(value = "can") String can); 
 
    @GetMapping("/device/gateway/uuid/{uuid}")
	public ResponseEntity<ResponseBodyDTO<Device>> getGatewayByUuid(@PathVariable("uuid") String uuid);
    
    @GetMapping("/device/can-status")
	public ResponseEntity<ResponseBodyDTO<List<Device>>> getGatewaysByAccountNumberAndStatus(@RequestParam(value = "logUuid", required = false) String logUuid, @RequestParam(value = "can") String accountNumber,
			@RequestParam(value = "status", required = false) String status);
    
//    @GetMapping("/device/product-name/{productName}")
//	public ResponseEntity<ResponseBodyDTO<List<Attribute>>> getAttributeListByProductName(@Validated
//			@PathVariable("productName") String producName);
    
    //------------------------------------------Aamir 1 end---------------------------------------//
	@GetMapping("/device/gateway-pagination")
	public ResponseEntity<Page<Device>> getGatewaysByAccountNumberAndStatusWithPagination(
			@RequestParam(value = "logUUid", required = false) String logUUid,
			@RequestParam(name = "install_code", required = false) String installCode,
			@RequestParam(value = "can", required = false) String accountNumber,
			@RequestParam(value = "_page", required = true) Integer page,
			@RequestParam(value = "_limit", required = true) Integer pageSize,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value="cans", required = false) List<String>  cans);
	
	 @GetMapping("/device/product-name/{productName}")
		public ResponseEntity<ResponseBodyDTO<List<Attribute>>> getAttributeListByProductName(@Validated @PathVariable("productName") String producName);
  
	 @GetMapping("/device/lookup")
	 ResponseEntity<String> getLookupValue(@RequestParam(name = "field") String field);
	 
	 @PutMapping("/device/sensor")
	 public ResponseEntity<ResponseBodyDTO<DeviceResponsePayload>> updateSensor(@Valid @RequestBody SensorUpdateRequest sensorUpdateRequest);
	 
	 @PutMapping("/device/update-sensor")
	    public ResponseEntity<ResponseBodyDTO<Device>> updateSensorBySensorObj(@Valid @RequestBody Device sensor);
	 
	 @GetMapping("/device/gateway-pagination-new")
		public ResponseEntity<Page<Device>> getGatewaysByAccountNumberAndStatusWithPaginationNew(
				@RequestParam(value = "can", required = false) String accountNumber,
				@RequestParam(value = "status", required = false) String status,
				@RequestParam(value = "_page", required = true) Integer page,
				@RequestParam(value = "_limit", required = true) Integer pageSize,
				@RequestParam(value = "_sort", required = false) String sort,
				@RequestParam(value = "_order", required = false) String order,
				@RequestParam(value="cans", required = false) List<String>  cans,
				@RequestParam(value = "time_of_last_download", required = false) String timeOfLastDownload);

	 
	 @GetMapping("/device/gateway-pagination-v2")
		public ResponseEntity<Page<GatewayDetailsBean>> getGatewaysByAccountNumberAndStatusWithPaginationV2(
				@RequestParam(value = "can", required = false) String accountNumber,
				@RequestParam(value = "status", required = false) String status,
				@RequestParam(value = "_page", required = true) Integer page,
				@RequestParam(value = "_limit", required = true) Integer pageSize,
				@RequestParam(value = "_sort", required = false) String sort,
				@RequestParam(value = "_order", required = false) String order,
				@RequestParam(value="cans", required = false) List<String>  cans,
				@RequestParam(value = "time_of_last_download", required = false) String timeOfLastDownload);
	 
	 @GetMapping("/gateway/assetsDetails")
		public ResponseEntity<ResponseBodyDTO<List<AssetsStatusPayload>>> getAssetsDetails(@RequestParam(value = "imei", required = false) String imei) ;

}
