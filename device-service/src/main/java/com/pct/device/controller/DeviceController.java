package com.pct.device.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;
import com.pct.common.controller.IApplicationController;
import com.pct.common.dto.AssetDTO;
import com.pct.common.dto.DeviceResponsePayloadForAssetUpdate;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.model.AssetSensorXref;
import com.pct.common.model.Attribute;
import com.pct.common.model.Device;
import com.pct.common.model.Device_Device_xref;
import com.pct.common.model.LatestDeviceReportCount;
import com.pct.common.model.Organisation;
import com.pct.common.payload.AssetSensorXrefPayload;
import com.pct.common.payload.DeviceSensorxrefPayload;
import com.pct.common.payload.GatewayDetailsBean;
import com.pct.common.payload.InstallationStatusGatewayRequest;
import com.pct.common.payload.SensorUpdateRequest;
import com.pct.common.payload.UpdateAssetToDeviceForInstallationRequest;
import com.pct.common.payload.UpdateGatewayAssetStatusRequest;
import com.pct.common.util.JwtUser;
import com.pct.common.util.Logutils;
import com.pct.device.dto.AttributeResponseDTO;
import com.pct.device.dto.DeviceListDTO;
import com.pct.device.dto.DeviceReportDTO;
import com.pct.device.dto.MessageDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.model.ColumnDefs;
import com.pct.device.model.DeviceReportCount;
import com.pct.device.payload.AssetDevicePayload;
import com.pct.device.payload.BatchDeviceEditPayload;
import com.pct.device.payload.DeviceCustomerUpdatePayload;
import com.pct.device.payload.DeviceDetailPayLoad;
import com.pct.device.payload.DeviceDetailsRequest;
import com.pct.device.payload.DeviceResponsePayload;
import com.pct.device.payload.DeviceWithSensorPayload;
import com.pct.device.payload.ExportDataPayload;
import com.pct.device.payload.UpdateDeviceStatusPayload;
import com.pct.device.repository.IDeviceRepository;
import com.pct.device.service.IAttributeValueService;
import com.pct.device.service.IProductMasterService;
import com.pct.device.service.impl.DeviceServiceImpl;
import com.pct.device.util.BeanConverter;
import com.pct.device.util.Context;
import com.pct.es.dto.Filter;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Data;

@RestController
@RequestMapping("/device")
@Api(value = "/device", tags = "Device Management")
public class DeviceController implements IApplicationController<Device> {
//	@Autowired
//private JwtUtil jwtUtil;
	
	public static final String className="DeviceController";

	@Autowired
	private DeviceServiceImpl deviceService;
	
	  

	@Autowired
	private IDeviceRepository deviceRepository;

    @Autowired
    IAttributeValueService attributeValueService;
    
    @Autowired
	IProductMasterService productMasterService;
    
    @Autowired
    BeanConverter beanConverter;

    
	Logger logger = LoggerFactory.getLogger(DeviceController.class);

	// ---------------------------------------------------Aamir
	// Start--------------------------------------------------------------------//
	  @PutMapping("/status/gateway-asset")
	    public ResponseEntity<ResponseBodyDTO<DeviceResponsePayload>>  updateGatewayAssetStatus(@RequestBody UpdateGatewayAssetStatusRequest updateGatewayAssetStatusRequest) {
		  String methodName="updateGatewayAssetStatus";
		  Context context =new Context();
		  String logUUid=context.getLogUUId();
	    	//logger.info("request recieved at device controller for finish install gateway update api gateway uuid and gateway status "+updateGatewayAssetStatusRequest.getGatewayUuid() +" "+updateGatewayAssetStatusRequest.getGatewayStatus());
		  Logutils.log(className,methodName,logUUid,"request recieved at device controller for finish install gateway update api gateway uuid and gateway status "+updateGatewayAssetStatusRequest.getGatewayUuid() +" "+updateGatewayAssetStatusRequest.getGatewayStatus(),logger);
		  Logutils.log(className,methodName,logUUid,"request device controller for finish install gateway update api asset uuid and asset status "+updateGatewayAssetStatusRequest.getAssetUuid() +" "+updateGatewayAssetStatusRequest.getAssetStatus(),logger);
	    	try {
	            Device gateway = deviceService.updateGatewayAssetStatus(updateGatewayAssetStatusRequest);
	            
	            
	            if (gateway != null) {
					Logutils.log(className,methodName,logUUid,"asset  Uuid : " + gateway.getUuid(),logger);
	            	DeviceResponsePayload deviceResponsePayload = new DeviceResponsePayload();
//	            	BeanUtils.copyProperties(gateway, deviceResponsePayload);
	            	deviceResponsePayload.setId(gateway.getId());
	            	deviceResponsePayload.setImei(gateway.getImei());
	                return new ResponseEntity<>(
	                        new ResponseBodyDTO<DeviceResponsePayload>(true, "Successfully updated Gateway and Asset status",
	                        		deviceResponsePayload), HttpStatus.OK);
	            } else {
	                logger.error("Exception while updating Gateway and Asset status");
	                return new ResponseEntity(
	                        new ResponseDTO(false, "Gateway not found for given gateway Id"), HttpStatus.INTERNAL_SERVER_ERROR);
	            }
	        } catch (Exception exception) {
	            logger.error("Exception while updating Gateway and Asset status", exception);
	            return new ResponseEntity(
	                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }
	 
//	@GetMapping("/product-name/{productName}")
//	public ResponseEntity<List<Attribute>> getAttributeListByProductName(@RequestParam(value = "logUUid", required = false) String logUUid, @Validated @PathVariable("productName") String producName) {
//		try {
//			List<Attribute> attributeResponse = productMasterService.getProductByName(producName);
//			return new ResponseEntity<>(attributeResponse, HttpStatus.OK);
//		} catch (DeviceException exception) {
//			logger.error("Exception occurred while getting attributeResponseList", exception);
//			return new ResponseEntity(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//		} catch (Exception exception) {
//			logger.error("Exception occurred while getting attributeResponseList", exception);
//			return new ResponseEntity(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}

	@GetMapping("/gateway-pagination")
	public ResponseEntity<Page<Device>> getGatewaysByAccountNumberAndStatusWithPagination(@RequestParam(value = "logUUid", required = false) String logUUid,
			@RequestParam(name = "install_code", required = false) String installCode, @RequestParam(value = "can", required = false) String accountNumber,
			@RequestParam(value = "status", required = false) String status, @RequestParam(value = "_page", required = true) Integer page,
			@RequestParam(value = "_limit", required = true) Integer pageSize, @RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order, @RequestParam(value = "cans", required = false) List<String> cans) {
		try {
			logger.info("gateway-pagination ============== sort is : " + sort + " order is : " + order + " status : " + status);
			if (sort == null || sort.equalsIgnoreCase("") || sort.equalsIgnoreCase("null")) {
				sort = null;
				order = null;
			} else if (order == null || order.equalsIgnoreCase("") || order.equalsIgnoreCase("null")) {
				sort = null;
				order = null;
			}
			logger.info("gateway-pagination ============== sort is : " + sort + " order is : " + order + " status : " + status);
			Page<Device> gatewayPage = deviceService.getGatewaysByAccountNumberAndStatusWithPagination(accountNumber, status,
					getPageable(page - 1, pageSize, sort, order), cans);
			return new ResponseEntity(gatewayPage, HttpStatus.OK);

		} catch (Exception exception) {
			logger.error("Exception while fetching Gateways", exception);
			return new ResponseEntity(new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/gateway-sensor-xref")
	public ResponseEntity<? extends Object> saveGatewaySensorXref(
			@Valid @RequestBody List<DeviceSensorxrefPayload> gatewaySensorXref) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("MessageUUID: " + gatewaySensorXref.get(0).getLogUUId()
					+ " Before getting response from saveGatewaySensorXref method from device controller :"
					+ stopWatch.prettyPrint());

			List<Device_Device_xref> gatewaySensorXrefObj = deviceService.saveGatewaySensorXref(gatewaySensorXref);

			logger.info("MessageUUID: " + gatewaySensorXref.get(0).getLogUUId()
					+ " After getting response from saveGatewaySensorXref method from device controller :"
					+ stopWatch.prettyPrint());

			return new ResponseEntity<>(new ResponseBodyDTO<List<Device_Device_xref>>(true,
					"Successfully updated GatewaySensorXref", gatewaySensorXrefObj), HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception while updating AssetSensorXref", exception);
			return new ResponseEntity(new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/sensor")
	public ResponseEntity<ResponseBodyDTO<DeviceResponsePayload>>  updateSensor(@Valid @RequestBody SensorUpdateRequest sensorUpdateRequest) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("MessageUUID: " + sensorUpdateRequest.getLogUUId()
					+ "Before getting response from updateSensor method from device controller :"
					+ stopWatch.prettyPrint());

			Device sensor = deviceService.updateSensor(sensorUpdateRequest);

			logger.info("MessageUUID: " + sensorUpdateRequest.getLogUUId()
					+ "After getting response from updateSensor method from device controller :"
					+ stopWatch.prettyPrint());

//			return new ResponseEntity<>(new ResponseBodyDTO<Device>(true, "Successfully updated Sensor", sensor),
//					HttpStatus.OK);
			DeviceResponsePayload deviceResponsePayload = new DeviceResponsePayload();
			// BeanUtils.copyProperties(sensor, deviceResponsePayload);
			deviceResponsePayload = beanConverter.deviceToDeviceResponsePayload(sensor);
        	
            return new ResponseEntity<>(
                    new ResponseBodyDTO<DeviceResponsePayload>(true, "Successfully updated Gateway and Asset status",
                    		deviceResponsePayload), HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception while updating Sensor", exception);
			return new ResponseEntity(new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/asset-sensor-xref")
	public ResponseEntity<? extends Object> updateAssetSensorXref(
			@Valid @RequestBody List<AssetSensorXrefPayload> assetSensorXrefPayload) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("MessageUUID: " + assetSensorXrefPayload.get(0).getLogUUId()
					+ " Before getting response from updateAssetSensorXref method from device controller :"
					+ stopWatch.prettyPrint());

			List<AssetSensorXref> assetSensorXrefObj = deviceService.updateAssetSensorXref(assetSensorXrefPayload);

			logger.info("MessageUUID: " + assetSensorXrefPayload.get(0).getLogUUId()
					+ " After getting response from updateAssetSensorXref method from device controller :"
					+ stopWatch.prettyPrint());
			AssetSensorXrefPayload assetSensorXrefPayload1 = new AssetSensorXrefPayload();
			return new ResponseEntity(new ResponseDTO(true, "AssetSensorXref updated successfully"), HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception while updating AssetSensorXref", exception);
			return new ResponseEntity(new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/can-mac")
	public ResponseEntity<ResponseBodyDTO<List<DeviceResponsePayload>>> getGatewayByMACAndCan(
			@RequestParam(value = "logUUId", required = true) String logUUId,
			@RequestParam(value = "mac", required = true) String mac,
			@RequestParam(value = "can", required = false) String can, HttpServletRequest httpServletRequest) {
		try {
			Device device = null;
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("MessageUUID: " + logUUId
					+ " Before getting response from getGatewayByMACAndCan method from device controller :"
					+ stopWatch.prettyPrint());

			if (can == null || can == "") {
				device = deviceService.getGatewayByMACAndCan(mac, null);
			} else {
				device = deviceService.getGatewayByMACAndCan(mac, can);
			}

			logger.info("MessageUUID: " + logUUId
					+ " After getting response from getGatewayByMACAndCan method from device controller :"
					+ stopWatch.prettyPrint());

			DeviceResponsePayload deviceResponsePayload = new DeviceResponsePayload();
			BeanUtils.copyProperties(device, deviceResponsePayload);
			List<DeviceResponsePayload> singlDdevice = new ArrayList();
			singlDdevice.add(deviceResponsePayload);
			return new ResponseEntity<ResponseBodyDTO<List<DeviceResponsePayload>>>(
					new ResponseBodyDTO<List<DeviceResponsePayload>>(true, "Fetched Device(s) Successfully",
							singlDdevice),
					HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception while getting asset for vin {}", mac, e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/update-asset")
	public ResponseEntity<ResponseBodyDTO<List<DeviceResponsePayload>>> updateAssetForGateway(
			@RequestParam(value = "logUUId", required = true) String logUUId,
			@RequestParam(value = "gatewayUuid", required = true) String gatewayUuid,
			@RequestParam(value = "assetUuid", required = true) String assetUuid) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("MessageUUID: " + logUUId
					+ " Before getting response from updateAssetForGateway method from device controller :"
					+ stopWatch.prettyPrint());

			Device device = deviceService.updateAssetForGateway(gatewayUuid, assetUuid);

			logger.info("MessageUUID: " + logUUId
					+ " After getting response from updateAssetForGateway method from device controller :"
					+ stopWatch.prettyPrint());

				DeviceResponsePayload deviceResponsePayload = new DeviceResponsePayload();	            
	            BeanUtils.copyProperties(device, deviceResponsePayload);	            
	            List<DeviceResponsePayload> singlDdevice = new ArrayList();
	            singlDdevice.add(deviceResponsePayload);
	            return new ResponseEntity<ResponseBodyDTO<List<DeviceResponsePayload>>>(
						new ResponseBodyDTO<List<DeviceResponsePayload>>(true, "Fetched Device(s) Successfully",
								singlDdevice),HttpStatus.OK);
	        } catch (Exception e) {
	            logger.error("Exception while updating asset id {} for gateway id {}",
	            		assetUuid, gatewayUuid, e);
	            return null;
	        }
	    }
	
	@PostMapping("/update-asset-v1")
	public ResponseEntity<ResponseBodyDTO<DeviceResponsePayloadForAssetUpdate>> updateAssetForGatewayV1(
			@RequestParam(value = "logUUId", required = true) String logUUId,
			@RequestParam(value = "gatewayUuid", required = true) String gatewayUuid,
			@RequestParam(value = "assetUuid", required = true) String assetUuid) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("MessageUUID: " + logUUId
					+ " Before getting response from updateAssetForGatewayNew method from device controller :"
					+ stopWatch.prettyPrint());

			DeviceResponsePayloadForAssetUpdate deviceResponsePayloadForAssetUpdate = deviceService.updateAssetForGatewayNew(gatewayUuid, assetUuid);

			logger.info("MessageUUID: " + logUUId
					+ " After getting response from updateAssetForGatewayNew method from device controller :"
					+ stopWatch.prettyPrint());

				
	            return new ResponseEntity<ResponseBodyDTO<DeviceResponsePayloadForAssetUpdate>>(
						new ResponseBodyDTO<DeviceResponsePayloadForAssetUpdate>(true, "Fetched Device(s) Successfully",
								deviceResponsePayloadForAssetUpdate),HttpStatus.OK);
	        } catch (Exception e) {
	            logger.error("Exception while updating asset id {} for gateway id {}",
	            		assetUuid, gatewayUuid, e);
	            return null;
	        }
	    }
	   
	    @PutMapping("/reset-install")
	    public ResponseEntity<ResponseDTO> resetInstall(@RequestParam(value = "logUUId", required = true) String logUUId, 
	    												@RequestParam(name = "assetId", required = false) Long assetId,
	                                                    @RequestParam(name = "gatewayId", required = false) Long gatewayId) {
	    	  String methodName ="resetInstallINDeviceController";
	    	try {
	    		StopWatch stopWatch = new StopWatch();
				stopWatch.start();
				logger.info("MessageUUID: "+ logUUId +" Before getting response from resetInstall method from device controller :" + stopWatch.prettyPrint());
				 
				String logUUid = new Context().getLogUUId();
				
				Boolean status = deviceService.resetInstall(logUUid, assetId, gatewayId);
	           
				logger.info("MessageUUID: "+ logUUId +" After getting response from resetInstall method from device controller :" + stopWatch.prettyPrint());
	            return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Reset Installation successfully"), HttpStatus.OK);
	        } catch (Exception exception) {
	            logger.error("Exception while resetting Gateway and Asset status", exception);
	            return new ResponseEntity<ResponseDTO>(
	                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }
	    @GetMapping("/can-imei")
	    public ResponseEntity<ResponseBodyDTO<DeviceResponsePayload>> getGatewayByImeiAndCan(@RequestParam(value = "logUUId", required = true) String logUUId,
	    																							@RequestParam(value = "imei", required = true) String imei,
	    																							@RequestParam(value = "can", required = true) String can) {
	        try {
	        	StopWatch stopWatch = new StopWatch();
				stopWatch.start();
	        	logger.info("MessageUUID: "+ logUUId +" Before getting response from getGatewayByImeiAndCan method from device controller :" + stopWatch.prettyPrint());
	        	
	        	Device device = deviceService.getGatewayByImeiAndCan(imei, can);
	        	
	        	logger.info("MessageUUID: "+ logUUId +" After getting response from getGatewayByImeiAndCan method from device controller :" + stopWatch.prettyPrint());
	        	
	        	DeviceResponsePayload deviceResponsePayload = new DeviceResponsePayload();
		        BeanUtils.copyProperties(device, deviceResponsePayload);
//		        List<DeviceResponsePayload> singlDdevice = new ArrayList();
//		        singlDdevice.add(deviceResponsePayload);
	            return new ResponseEntity<ResponseBodyDTO<DeviceResponsePayload>>(
							new ResponseBodyDTO<DeviceResponsePayload>(true, "Fetched Device(s) Successfully",
									deviceResponsePayload),HttpStatus.OK);
	        } catch (Exception e) {
	            logger.error("Exception while getting asset for vin {}", imei, e);
	            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }
	    
	    @GetMapping("/asset/uuid/delete/{assetUuid}")
	    public ResponseEntity<? extends Object> deleteAssetByAssetUuid(@RequestParam(value = "logUUid", required = false) String logUUid, @PathVariable("assetUuid") String assetUuid) {
	        try {
	            Boolean isDeleted = deviceService.deleteAssetByAssetUuid(assetUuid);
	            return new ResponseEntity<>(isDeleted, HttpStatus.OK);
	        } catch (Exception e) {
	            logger.error("Exception while getting asset for uuid {}", assetUuid, e);
	            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }

	    @GetMapping("/asset")
	    public  ResponseEntity<ResponseBodyDTO<List<AssetDTO>>> getAssetsByAccountNumberAndStatusUsingDTO(@RequestParam(value = "logUUid", required = false) String logUUid, @RequestParam(value = "can") String accountNumber, @RequestParam(value = "status", required = false) String status) {
	        try {
	            List<AssetDTO> assets = deviceService.getAssetsByAccountNumberAndStatusUsingDTO(accountNumber, status);//	       
	            return new ResponseEntity<ResponseBodyDTO<List<AssetDTO>>>(
						new ResponseBodyDTO<List<AssetDTO>>(true, "Successfully fetched Assets",
								assets),
						HttpStatus.OK);
	        } catch (Exception exception) {
	            logger.error("Exception while fetching Assets", exception);
	            return new ResponseEntity(
	                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }
	    @GetMapping("/get-attribute-value")
	   	public ResponseEntity<List<AttributeResponseDTO>> getAllInstallatioDetails(@RequestParam(value = "deviceId", required = true) String deviceId,
	   											  HttpServletRequest httpServletRequest) {
	     try {
	    	 List<AttributeResponseDTO> details = attributeValueService.getAttributeValueByGatewayDeviceId(deviceId);
	    	 return new ResponseEntity<>(details, HttpStatus.OK);
	     } catch (Exception e) {
	         logger.error("Exception occurred while getting attribute values", e);
	         return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	     }
	   	}
	    
	    @GetMapping("/lookup")
	    public ResponseEntity<String> getLookupValue(@RequestParam(name = "field") String field) {
	        try {
	        	logger.info(" Before getting response from getLookupValue method from device controller :");
	            String value = deviceService.getLookupValue(field);
	            logger.info(" After getting response from getLookupValue method from device controller :");
	            return new ResponseEntity<String>(value, HttpStatus.OK);
	        } catch(Exception e) {
	            logger.error("Exception while getting lookup value for field {}", field, e);
	            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }
	    //-------------------------------------------------Aamir End----------------------------------------------------------------------//
	@ApiOperation(value = "Get device detail(s)", notes = "API to get device detail(s)", response = DeviceListDTO.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get it", response = DeviceListDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping()
	public ResponseEntity<ResponseBodyDTO<List<DeviceResponsePayload>>> getDevice(HttpServletRequest httpServletRequest,
			@RequestParam(value = "logUUId", required = false) String logUUId,
			@RequestParam(value = "can", required = false) String accountNumber,
			@RequestParam(value = "device-uuid", required = false) String uuid,
			@RequestParam(value = "device-id", required = false) String deviceId) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info(
					"MessageUUID: " + logUUId + "Before getting response from getDevice method from device controller :"
							+ stopWatch.prettyPrint());
			List<DeviceResponsePayload> deviceList = deviceService.getDeviceDetails(accountNumber, uuid, deviceId);
			stopWatch.stop();
			logger.info(
					"MessageUUID: " + logUUId + "After getting response from getDevice method from device controller :"
							+ stopWatch.prettyPrint());
			return new ResponseEntity<ResponseBodyDTO<List<DeviceResponsePayload>>>(
					new ResponseBodyDTO<List<DeviceResponsePayload>>(true, "Fetched Device(s) Successfully",
							deviceList),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting device(s)", exception);
			return new ResponseEntity<>(new ResponseBodyDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get parsed report", notes = "API to get  parsed report", response = ResponseBodyDTO.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get it", response = ResponseBodyDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/parse-report")
	public ResponseEntity<ResponseBodyDTO> getParsedReport(HttpServletRequest httpServletRequest,
			@RequestParam(value = "raw-report", required = false) String rawReport,
			@RequestParam(value = "format", required = false) String format,
			@RequestParam(value = "type", required = false) String type) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Before getting response from getParseReport method from device controller :"
					+ stopWatch.prettyPrint());
			String parsedReport = deviceService.getParsedReport(rawReport, format, type);
			stopWatch.stop();
			logger.info("After getting response from getParseReport method from device controller :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity<ResponseBodyDTO>(
					new ResponseBodyDTO<>(true, "Fetched Parsed Report Successfully", parsedReport), HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting parsed report", exception);
			return new ResponseEntity<>(new ResponseBodyDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get device list with pagination", notes = "API to get device list  with pagination", response = Object.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get with pagination", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/page")
	public ResponseEntity<Object> getDeviceListWithPagination(
			@RequestParam(value = "can", required = false) String accountNumber,
			@RequestParam(value = "imei", required = false) String imei,
			@RequestParam(value = "device-uuid", required = false) String deviceUuid,
			@RequestParam(value = "device-status", required = false) DeviceStatus deviceStatus,
			@RequestParam(value = "device-type", required = false) IOTType type,
			@RequestParam(value = "macAddress", required = false) String macAddress,
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "time_of_last_download", required = false) String timeOfLastDownload,
			@RequestParam(value = "_filterModelCountFilter", required = false) String filterModelCountFilter,
			@RequestBody Map<String, Filter> filterValues, HttpServletRequest httpServletRequest) {
		try {
			String messageUuid = UUID.randomUUID().toString();
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Before getting response from getDeviceListWithPagination method from device controller :"
					+ stopWatch.prettyPrint());
			logger.info("Inside getDeviceListWithPagination Message Uuid is : " + messageUuid);
			MessageDTO<Page<DeviceResponsePayload>> messageDto = new MessageDTO<>("Fetched Device(s) Successfully",
					true);
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			Page<DeviceResponsePayload> device = deviceService.getDeviceWithPagination(messageUuid,accountNumber, imei, deviceUuid,
					deviceStatus, type, macAddress, filterValues, filterModelCountFilter,
					getPageable(page - 1, size, sort, order), jwtUser.getUsername(), false, token, timeOfLastDownload,sort);
			messageDto.setBody(device);
			messageDto.setTotalKey(device.getTotalElements());
			logger.info("Inside getGatewayList Post Size ==== " + device.getTotalElements());
			logger.info("Current Page " + device.getNumber());
			logger.info("Total pages" + device.getTotalPages());
			stopWatch.stop();
			logger.info("After getting response from getDeviceListWithPagination method from device controller :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity<>(messageDto, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting device(s)", exception);
			return new ResponseEntity<Object>(new ResponseBodyDTO<DeviceListDTO>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get device with sensor list with pagination", notes = "API to get device  with sensor list  with pagination", response = Object.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get with pagination", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/device-list")
	public ResponseEntity<Object> getDeviceAndSensorListWithPagination(
			@RequestParam(value = "can", required = false) String accountNumber,
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "_filterModelCountFilter", required = false) String filterModelCountFilter,
			@RequestBody Map<String, String> filterValues, HttpServletRequest httpServletRequest) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info(
					"Before getting response from getDeviceAndSensorListWithPagination method from device controller :"
							+ stopWatch.prettyPrint());
			MessageDTO<Page<DeviceWithSensorPayload>> messageDto = new MessageDTO<>("Fetched Device(s) Successfully",
					true);
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			Page<DeviceWithSensorPayload> device = deviceService.getDeviceAndSensorWithPagination(accountNumber,
					filterValues, filterModelCountFilter, getPageable(page - 1, size, sort, order),
					jwtUser.getUsername(),sort);
			messageDto.setBody(device);
			messageDto.setTotalKey(device.getTotalElements());
			logger.info("Inside getGatewayList Post Size ==== " + device.getTotalElements());
			logger.info("Current Page " + device.getNumber());
			logger.info("Total pages" + device.getTotalPages());
			stopWatch.stop();
			logger.info(
					"After getting response from getDeviceAndSensorListWithPagination method from device controller :"
							+ stopWatch.prettyPrint());
			return new ResponseEntity<>(messageDto, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting device(s)", exception);
			return new ResponseEntity<Object>(new ResponseBodyDTO<DeviceListDTO>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Update device status", notes = "API to update device status", response = String.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Updated Successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PutMapping("/update-status")
	public ResponseEntity<ResponseDTO> updateDeviceStatus(
			@Valid @RequestBody UpdateDeviceStatusPayload deviceStatusUpdateRequest) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Before getting response from updateDeviceStatus method from device controller :"
					+ stopWatch.prettyPrint());
			deviceService.updateDeviceStatus(deviceStatusUpdateRequest);
			logger.info("After getting response from updateDeviceStatus method from device controller :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "Device status successfully updated"),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception while updating device status", exception);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Add device detail", notes = "API to add device", response = String.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Added Successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping()
	public ResponseEntity<ResponseDTO> addDeviceDetail(@RequestBody DeviceDetailsRequest deviceUploadRequest,
			HttpServletRequest httpServletRequest) {
		Boolean status = false;
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			logger.info("Request received to add device detail {}", deviceUploadRequest.toString());
			status = deviceService.addDeviceDetail(deviceUploadRequest, jwtUser.getUsername());
		} catch (Exception e) {
			logger.error("Exception occurred in adding of device detail", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Device details saved successfully"),
				HttpStatus.CREATED);
	}

	@ApiOperation(value = "Update device detail", notes = "API to update device detail", response = String.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Updated device successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PutMapping()
	public ResponseEntity<ResponseDTO> updateDeviceDetail(@RequestBody DeviceDetailPayLoad devicedetailPayload,
			HttpServletRequest httpServletRequest) {
		Boolean status = false;
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			logger.info("Request received to update device detail {}", devicedetailPayload.toString());
			status = deviceService.updateDeviceDetail(devicedetailPayload, jwtUser.getUsername());
		} catch (Exception e) {
			logger.error("Exception occurred in updating of device detail", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Device details updated successfully"),
				HttpStatus.CREATED);
	}

	@ApiOperation(value = "Delete the device details", notes = "API to delete the device details", response = String.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Success", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@DeleteMapping()
	public ResponseEntity<ResponseDTO> deleteDeviceDetail(@RequestParam(value = "_can", required = false) String can,
			@RequestParam(value = "_imei", required = false) String imei,
			@RequestParam(value = "_uuid", required = false) String uuid,
			@RequestParam(value = "_type", required = false) IOTType type) {
		Boolean status = false;
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Request received to delete device detail {} in device controller" + stopWatch.prettyPrint());
			status = deviceService.deleteDeviceDetail(can, imei, uuid, type);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Device deleted successfully"),
					HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/type/{type}")
	public ResponseEntity<String[]> getCompanyForType(@PathVariable("type") String type) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Inside getCompanyForType method in device controller :" + stopWatch.prettyPrint());
			String[] companies = deviceService.getCompanyByType(type);
			return new ResponseEntity<String[]>(companies, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Company", exception);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/company")
	public ResponseEntity<Organisation> getCompanyById(@RequestParam(value = "id", required = true) Long id) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Inside getCompanyForType method in device controller :" + stopWatch.prettyPrint());
			Organisation companies = deviceService.getCompanyById(id);
			return new ResponseEntity<Organisation>(companies, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Company", exception);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/company/type/{type}")
	public ResponseEntity<List<Organisation>> getListOfCompanyForType(@PathVariable("type") String type,
			@RequestParam(value = "name", required = false) String name) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Inside getCompanyForType method in device controller :" + stopWatch.prettyPrint());
			List<Organisation> companies = deviceService.getListOfCompanyByType(type, name);
			return new ResponseEntity<List<Organisation>>(companies, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Company", exception);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Data
	class serachDto {
		private SearchHit[] serach;
		private Long totalKey;
	}

	@PostMapping("/getDeviceFromElastic")
	public SearchHits getDeviceFromElastic(@RequestParam int from, @RequestParam int size,
			@RequestParam(required = false) String order, @RequestParam(required = true) String deviceId,
			@RequestParam(required = false) String sort, @RequestParam(required = false) String column,
			@RequestBody Map<String, Filter> filterValues) {
		SearchResponse elasticData = null;
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Before getDeviceFromElastic method in device controller :" + stopWatch.prettyPrint());
			elasticData = deviceService.getDeviceFromElastic(from, size, filterValues, deviceId, sort, order);
			logger.info("Elastic Data Total Hits" + elasticData.getHits().getTotalHits());
			SearchHits search = elasticData.getHits();
			SearchHit[] serach = search.getHits();
			serachDto ser = new serachDto();
			MessageDTO<serachDto> messageDto = new MessageDTO<>("Fetched device report(s) successfully", true);
			ser.setSerach(serach);
			ser.setTotalKey(elasticData.getHits().getTotalHits().value);
			messageDto.setBody(ser);
			logger.info("After getting response from getDeviceFromElastic method in device controller method :");
			messageDto.setTotalKey(ser.getTotalKey());
			return search;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@GetMapping("/report-columndefs")
	public ResponseEntity<List<ColumnDefs>> getColumnDefsForReport() {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Inside getColumnDefsForReport method in device controller :" + stopWatch.prettyPrint());
			List<ColumnDefs> columnDefs = deviceService.getColumnDefs();
			return new ResponseEntity<List<ColumnDefs>>(columnDefs, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Company", exception);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/getDeviceReportByUUid")
	public SearchHits getDeviceFromElasticByUUid(@RequestParam int from, @RequestParam int size,
			@RequestParam(required = false) String order, @RequestParam(required = false) String deviceId,
			@RequestParam(required = false) String sort, @RequestParam(required = false) String column,
			@RequestBody Map<String, Filter> filterValues) {
		SearchResponse elasticData = null;
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Before getDeviceFromElastic method in device controller :" + stopWatch.prettyPrint());
			elasticData = deviceService.getDeviceReportByUUid(from, size, filterValues, deviceId);
			logger.info("Elastic Data Total Hits" + elasticData.getHits().getTotalHits());
			SearchHits search = elasticData.getHits();
			SearchHit[] serach = search.getHits();
			serachDto ser = new serachDto();
			MessageDTO<serachDto> messageDto = new MessageDTO<>("Fetched device report(s) successfully", true);
			ser.setSerach(serach);
			ser.setTotalKey(elasticData.getHits().getTotalHits().value);
			messageDto.setBody(ser);
			logger.info("After getting response from getDeviceFromElastic method in device controller method :");
			messageDto.setTotalKey(ser.getTotalKey());
			return search;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("/getDeviceReportFromElastic")
	public SearchHits getDeviceReportFromElastic(@RequestParam int from, @RequestParam int size,
			@RequestParam(required = false) String order, @RequestParam(required = false) String sort,
			@RequestParam(required = false) String column, @RequestBody Map<String, Filter> filterValues) {
		SearchResponse elasticData = null;
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Before getDeviceFromElastic method in device controller :" + stopWatch.prettyPrint());
			elasticData = deviceService.getDeviceReportFromElastic(from, size, filterValues, sort, order);
			logger.info("Elastic Data Total Hits" + elasticData.getHits().getTotalHits());
			SearchHits search = elasticData.getHits();
			SearchHit[] serach = search.getHits();
			serachDto ser = new serachDto();
			MessageDTO<serachDto> messageDto = new MessageDTO<>("Fetched device report(s) successfully", true);
			ser.setSerach(serach);
			ser.setTotalKey(elasticData.getHits().getTotalHits().value);
			messageDto.setBody(ser);
			logger.info("After getting response from getDeviceFromElastic method in device controller method :");
			messageDto.setTotalKey(ser.getTotalKey());
			return search;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("/device-reports")
	public List<Map<String, Object>> getDeviceReport(@RequestParam(value = "from", required = false) Integer from,
			@RequestParam(value = "size", required = false) Integer size,
			@RequestParam(value = "imei", required = false) String imei,
			@RequestParam(value = "companyId", required = false) String companyId,
			@RequestParam(value = "startTime", required = false) String startTime,
			@RequestParam(value = "endTime", required = false) String endTime) {
		List<Map<String, Object>> elasticData = null;
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Beforse getDeviceFromElastic method in device controller :" + stopWatch.prettyPrint());
			elasticData = deviceService.getDeviceReport(from, size, imei, companyId, startTime, endTime);
			logger.info("After getting response from getDeviceFromElastic method in device controller method :");
			return elasticData;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@ApiOperation(value = "Update asset device detail", notes = "API to update asset device detail", response = String.class, tags = {
			"Device Management" })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Updated asset & device details successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PutMapping("/update-asset-device")
	public ResponseEntity<ResponseDTO> updateAssetDeviceDetails(@RequestBody AssetDevicePayload assetDevicePayload,
			HttpServletRequest httpServletRequest) {
		Boolean status = false;
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			logger.info("Request received to update device detail {}", assetDevicePayload.toString());
			status = deviceService.updateAssetDeviceDetails(assetDevicePayload, jwtUser.getUsername());
		} catch (Exception e) {
			logger.error("Exception occurred in updating of device detail", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Device details updated successfully"),
				HttpStatus.CREATED);
	}

	@ApiOperation(value = "Update  Device customer ", notes = "API to update  Device customer ", response = String.class, tags = {
			"Device Management" })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Updated Device customer successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PutMapping("/update-device-customer")
	public ResponseEntity<ResponseDTO> updateDeviceCustomer(
			@RequestBody DeviceCustomerUpdatePayload deviceCustomerUpdatePayload,
			HttpServletRequest httpServletRequest) {
		Boolean status = false;
		try {
			logger.info("Request received to update device customer {}", deviceCustomerUpdatePayload.toString());
			status = deviceService.updateDeviceCustomerDetails(deviceCustomerUpdatePayload);
		} catch (Exception e) {
			logger.error("Exception occurred in updating of device customer", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Device customer updated successfully"),
				HttpStatus.CREATED);
	}

	@SuppressWarnings("unchecked")
	@ApiOperation(value = "Get device list with pagination", notes = "API to get device list  with pagination", response = Object.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get with pagination", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/exportToCsvWithFilterForDevice")
	public void exportToCSVDeviceListWithPagination(@RequestParam(value = "can", required = false) String accountNumber,
			@RequestParam(value = "imei", required = false) String imei,
			@RequestParam(value = "device-uuid", required = false) String deviceUuid,
			@RequestParam(value = "device-status", required = false) DeviceStatus deviceStatus,
			@RequestParam(value = "device-type", required = false) IOTType type,
			@RequestParam(value = "macAddress", required = false) String macAddress,
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "_filterModelCountFilter", required = false) String filterModelCountFilter,
			@RequestBody ExportDataPayload filterObj, HttpServletRequest httpServletRequest,
			HttpServletResponse response) throws Exception {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			String messageUuid = UUID.randomUUID().toString();
			logger.info("Inside exportToCSVDeviceListWithPagination MessageUuid : " +messageUuid);
					
			logger.info("Before getting response from getDeviceListWithPagination method from device controller :"
					+ stopWatch.prettyPrint());
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");
			if (size > 10000) {
				final Map<String, Filter> filterValues2 = filterObj.getFilterValue();
				final List<String> columnDef2 = filterObj.getHeaderName();
				Runnable myThread = () -> {
					deviceService.exportDeviceDataIntoCSV(messageUuid,accountNumber, imei, deviceUuid, deviceStatus, type,
							macAddress, filterValues2, filterModelCountFilter, page, size, sort, order,
							jwtUser.getUsername(), true, token, columnDef2, response);
				};
				Thread run = new Thread(myThread);
				run.start();
			} else {
				deviceService.exportDeviceDataIntoCSV(messageUuid,accountNumber, imei, deviceUuid, deviceStatus, type, macAddress,
						filterObj.getFilterValue(), filterModelCountFilter, page, size, sort, order,
						jwtUser.getUsername(), true, token, filterObj.getHeaderName(), response);
			}
//			return new ResponseEntity<>(messageDto, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting device(s)", exception);
			throw exception;
//			return new ResponseEntity<Object>(new ResponseBodyDTO<DeviceListDTO>(false, exception.getMessage(), null),
//					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/exportDataFromCsv")
	public void exportDeviceReportFromElastic(HttpServletResponse response, @RequestParam int from,
			@RequestParam int size, @RequestParam(required = false) String order,
			@RequestParam(required = false) String sort, @RequestParam(required = false) String column,
			@RequestParam(required = false) String deviceId, @RequestBody ExportDataPayload filterObj) {
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
					.getHeader("Authorization");

			if (size > 10000) {
				final Map<String, Filter> filterValues2 = filterObj.getFilterValue();
				final List<String> columnDef2 = filterObj.getHeaderName();
				Runnable myThread = () -> {

					deviceService.getCSVData(response, from, size, order, sort, column, filterValues2, deviceId,
							jwtUser.getUsername(), token, columnDef2);
				};
				Thread run = new Thread(myThread);
				run.start();
			} else {
				deviceService.getCSVData(response, from, size, order, sort, column, filterObj.getFilterValue(),
						deviceId, jwtUser.getUsername(), token, filterObj.getHeaderName());
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception occurred while getting all device report", e);
			throw new DeviceException(e.getMessage());
		}
	}

	@ApiOperation(value = "Add device detail", notes = "API to add device", response = String.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Added Successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("uploadDevice")
	public ResponseEntity<ResponseDTO> addDeviceDetailInBatch(
			@RequestBody List<DeviceDetailsRequest> deviceUploadRequest, HttpServletRequest httpServletRequest) {
		Boolean status = false;
		try {
//			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//			logger.info("Username : " + jwtUser.getUsername());
//			logger.info("Request received to add device detail {}", deviceUploadRequest.toString());
			status = deviceService.addDeviceDetailInBatch(deviceUploadRequest);
		} catch (Exception e) {
			logger.error("Exception occurred in adding of device detail", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Device details saved successfully"),
				HttpStatus.CREATED);
	}

	@ApiOperation(value = "Get latest device report count list with pagination", notes = "API to get latest device report count list  with pagination", response = Object.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get with pagination", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/report/page")
	public ResponseEntity<Object> getLatestDeviceReportCountWithPagination(
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "_filterModelCountFilter", required = false) String filterModelCountFilter,
			@RequestBody Map<String, Filter> filterValues, HttpServletRequest httpServletRequest) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info(
					"Before getting response from getLatestDeviceReportCountWithPagination method from device controller :"
							+ stopWatch.prettyPrint());
			MessageDTO<Page<LatestDeviceReportCount>> messageDto = new MessageDTO<>(
					"Fetched latest Device(s) report count Successfully", true);
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			Page<LatestDeviceReportCount> device = deviceService.getLatestDeviceReportCountWithPagination(filterValues,
					filterModelCountFilter, getPageable(page - 1, size, sort, order), jwtUser.getUsername());
			messageDto.setBody(device);
			messageDto.setTotalKey(device.getTotalElements());
			logger.info("Inside getLatestDeviceReportCountWithPagination Post Size ==== " + device.getTotalElements());
			logger.info("Current Page " + device.getNumber());
			logger.info("Total pages" + device.getTotalPages());
			stopWatch.stop();
			logger.info(
					"After getting response from getLatestDeviceReportCountWithPagination method from device controller :"
							+ stopWatch.prettyPrint());
			return new ResponseEntity<>(messageDto, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting LatestDeviceReportCountWithPagination(s)", exception);
			return new ResponseEntity<Object>(new ResponseBodyDTO<DeviceListDTO>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	
	@ApiOperation(value = "Get latest device report count list with pagination", notes = "API to get latest device report count list  with pagination", response = Object.class, tags = {
	"Device Management" })
@ApiResponses(value = { @ApiResponse(code = 201, message = "get with pagination", response = Object.class),
	@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
	@ApiResponse(code = 403, message = "Forbidden"),
	@ApiResponse(code = 500, message = "Internal Server Error") })
@PostMapping("/report/counts")
public String getDeviceReportCountWithPaginations(
	@RequestParam(value = "_page", required = false) Integer page,
	@RequestParam(value = "_limit", required = false) Integer size,
	@RequestParam(value = "_sort", required = false) String sort,
	@RequestParam(value = "_order", required = false) String order,
	@RequestParam(value = "_filterModelCountFilter", required = false) String filterModelCountFilter,
	@RequestBody Map<String, Filter> filterValues, HttpServletRequest httpServletRequest) {
try {
	StopWatch stopWatch = new StopWatch();
	stopWatch.start();
	page=1;
	size=25;
	sort="general_mask_fields.received_time_stamp";
	order="desc";
	logger.info(
			"Before getting response from getDeviceReportCountWithPaginations method from device controller :"
					+ stopWatch.prettyPrint());
	MessageDTO<Page<DeviceReportCount>> messageDto = new MessageDTO<>(
			"Fetched latest Device(s) report count Successfully", true);
	JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	logger.info("Username : " + jwtUser.getUsername());
	Page<DeviceReportCount> device = deviceService.getDeviceReportCountWithPagination(filterValues,
			filterModelCountFilter, getPageable(page - 1, size, sort, order), jwtUser.getUsername());
	messageDto.setBody(device);
	messageDto.setTotalKey(device.getTotalElements());
	logger.info("Inside getLatestDeviceReportCountWithPagination Post Size ==== " + device.getTotalElements());
	logger.info("Current Page " + device.getNumber());
	logger.info("Total pages" + device.getTotalPages());
	stopWatch.stop();
	logger.info(
			"After getting response from getLatestDeviceReportCountWithPagination method from device controller :"
					+ stopWatch.prettyPrint());
	return Long.toString(device.getTotalElements());
} catch (Exception exception) {
	logger.error("Exception occurred while getting LatestDeviceReportCountWithPagination(s)", exception);
	return "error";
}
}
	
	@ApiOperation(value = "Get latest device report count list with pagination", notes = "API to get latest device report count list  with pagination", response = Object.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get with pagination", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/report/count/page")
	public ResponseEntity<Object> getReportCountWithPagination(
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "_filterModelCountFilter", required = false) String filterModelCountFilter,
			@RequestBody Map<String, Filter> filterValues, HttpServletRequest httpServletRequest) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info(
					"Before getting response from getLatestDeviceReportCountWithPagination method from device controller :"
							+ stopWatch.prettyPrint());
			MessageDTO<Page<LatestDeviceReportCount>> messageDto = new MessageDTO<>(
					"Fetched latest Device(s) report count Successfully", true);
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			Page<LatestDeviceReportCount> device = deviceService.getLatestDeviceReportCountWithPagination(filterValues,
					filterModelCountFilter, getPageable(page - 1, size, sort, order), jwtUser.getUsername());
			messageDto.setBody(device);
			messageDto.setTotalKey(device.getTotalElements());
			logger.info("Inside getLatestDeviceReportCountWithPagination Post Size ==== " + device.getTotalElements());
			logger.info("Current Page " + device.getNumber());
			logger.info("Total pages" + device.getTotalPages());
			stopWatch.stop();
			logger.info(
					"After getting response from getLatestDeviceReportCountWithPagination method from device controller :"
							+ stopWatch.prettyPrint());
			return new ResponseEntity<>(messageDto, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting LatestDeviceReportCountWithPagination(s)", exception);
			return new ResponseEntity<Object>(new ResponseBodyDTO<DeviceListDTO>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get latest device report count list with pagination", notes = "API to get latest device report count list  with pagination", response = Object.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get with pagination", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/report/count")
	public ResponseEntity<Object> getDeviceReportCountWithPagination(
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "_filterModelCountFilter", required = false) String filterModelCountFilter,
			@RequestBody Map<String, Filter> filterValues, HttpServletRequest httpServletRequest) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info(
					"Before getting response from getLatestDeviceReportCountWithPagination method from device controller :"
							+ stopWatch.prettyPrint());
			MessageDTO<Page<DeviceReportCount>> messageDto = new MessageDTO<>(
					"Fetched latest Device(s) report count Successfully", true);
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			Page<DeviceReportCount> device = deviceService.getDeviceReportCountWithPagination(filterValues,
					filterModelCountFilter, getPageable(page - 1, size, sort, order), jwtUser.getUsername());
			messageDto.setBody(device);
			messageDto.setTotalKey(device.getTotalElements());
			logger.info("Inside getLatestDeviceReportCountWithPagination Post Size ==== " + device.getTotalElements());
			logger.info("Current Page " + device.getNumber());
			logger.info("Total pages" + device.getTotalPages());
			stopWatch.stop();
			logger.info(
					"After getting response from getLatestDeviceReportCountWithPagination method from device controller :"
							+ stopWatch.prettyPrint());
			return new ResponseEntity<>(messageDto, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting LatestDeviceReportCountWithPagination(s)", exception);
			return new ResponseEntity<Object>(new ResponseBodyDTO<DeviceListDTO>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get device imei ids using customer account number", notes = "API to get device imei ids using customer account number", response = ResponseBodyDTO.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "get it", response = DeviceListDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/imei")
	public ResponseEntity<ResponseBodyDTO<Set<String>>> getDeviceImeisByCustomerAccountNumber(
			HttpServletRequest httpServletRequest, @RequestParam(value = "can") String accountNumber) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info(
					"Before getting response from getDeviceImeisByCustomerAccountNumber method from device controller :"
							+ stopWatch.prettyPrint());
			Set<String> imeis = deviceService.getDeviceImeisByCustomerAccountNumber(accountNumber);
			stopWatch.stop();
			logger.info(
					"After getting response from getDeviceImeisByCustomerAccountNumber method from device controller :"
							+ stopWatch.prettyPrint());
			return new ResponseEntity<>(new ResponseBodyDTO<>(true, "Fetched Device imei(s) Successfully", imeis),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting device imei(s)", exception);
			return new ResponseEntity<>(new ResponseBodyDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/download")
	public ResponseEntity<Resource> download(@RequestParam(value = "fileName", required = true) String fileName)
			throws IOException {
		try {
			File file = new File("files/" + fileName);

			HttpHeaders header = new HttpHeaders();
			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			Path path = Paths.get(file.getAbsolutePath());
			ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

			return ResponseEntity.ok().headers(header).contentLength(file.length())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		} catch (NoSuchFileException e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new DeviceException(fileName + " not available");
		} finally {
			try {
				// File file = new File("files/"+fileName);
				// file.delete();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@ApiOperation(value = "Get Device Count by Organization Id", notes = "API to get count of device based on organization", response = Object.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get with pagination", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/count_by_org_id")
	public ResponseEntity<Object> getDeviceCount(@RequestBody List<String> ids, HttpServletRequest httpServletRequest) {
		try {
			return new ResponseEntity<>(String.valueOf(deviceService.getDeviceCountByOrganizationId(ids)),
					HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception while getting device count", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get all device company mappings", notes = "API to get all company mappings", response = ResponseBodyDTO.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "get it", response = DeviceListDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/company/mappings")
	public ResponseEntity<ResponseBodyDTO<Map<String, String>>> getAllDeviceCompanyMapping(
			HttpServletRequest httpServletRequest) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Before getting response from getAllDeviceCompanyMapping method from device controller :"
					+ stopWatch.prettyPrint());
			Map<String, String> mappingMap = deviceService.getAllDeviceCompanyMapping();
			stopWatch.stop();
			logger.info("After getting response from getAllDeviceCompanyMapping method from device controller :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity<>(
					new ResponseBodyDTO<>(true, "Fetched Device company mapping(s) Successfully", mappingMap),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting device company mappings", exception);
			return new ResponseEntity<>(new ResponseBodyDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get byte data of downloaded image", notes = "API to get downloaded image", response = Resource.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "get it", response = Resource.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping(value = "/image-download")
	public ResponseEntity<Resource> downloadImage(@RequestParam(value = "fileName", required = true) String fileName)
			throws IOException {
		try {
			Resource resource = deviceService.downloadImage(fileName);
			return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream"))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
					.body(resource);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DeviceException(e.getMessage());
		}
	}
	@PutMapping("/status/device-asset")
	public ResponseEntity<? extends Object> updateDeviceAssetStatus(
			@Valid @RequestBody UpdateGatewayAssetStatusRequest updateDeviceAssetStatusRequest) {
		logger.info(
				"request recieved at device controller for finish install gateway update api gateway uuid and gateway status "
						+ updateDeviceAssetStatusRequest.getGatewayUuid() + " "
						+ updateDeviceAssetStatusRequest.getGatewayStatus());
		logger.info("request device controller for finish install gateway update api asset uuid and asset status "
				+ updateDeviceAssetStatusRequest.getAssetUuid() + " "
				+ updateDeviceAssetStatusRequest.getAssetStatus());
		try {
			Device device = deviceService.updateDeviceAssetStatus(updateDeviceAssetStatusRequest);
			if (device != null) {
				return new ResponseEntity<>(
						new ResponseBodyDTO<Device>(true, "Successfully updated Gateway and Asset status", device),
						HttpStatus.OK);
			} else {
				logger.error("Exception while updating Gateway and Asset status");
				return new ResponseEntity(new ResponseDTO(false, "Gateway not found for given gateway Id"),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception exception) {
			logger.error("Exception while updating Gateway and Asset status", exception);
			return new ResponseEntity(new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/ms-device/assetToDevice")
	public ResponseEntity<ResponseBodyDTO<Long>> updateAssetToDeviceInMS(
			@RequestBody UpdateAssetToDeviceForInstallationRequest request) {
		try {
			Long recordId = deviceService.updateAssetToDeviceInMS(request);
			return new ResponseEntity<>(
					new ResponseBodyDTO(false, "Successfully updated AssetToDevice in MS", recordId), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception while getting devices from MS database {}", e);
			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	
	 @PutMapping("/gateway/status")
	    public ResponseEntity<ResponseBodyDTO<DeviceResponsePayload>> updateGatewayStatus(@Valid @RequestBody InstallationStatusGatewayRequest installationStatusGatewayRequest) {
		 logger.info("Inside updateGatewayStatus Method From DeviceController");
		 Context context = new Context();
		 String methodName = "updateGatewayStatus";
	        try {
	        	
	        	//Logutils.logger(className,methodName, context.getLogUUId(), "Before calling updateGatewayStatus method from deviceService", context.getLogUUId(), InstallationStatusGatewayRequest.class);
	        	JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	        	
	        	//Logutils.logger(className,methodName,context.getLogUUId()," Before calling updateGatewayStatus method from assetService",logger, context.getLogUUId(), installationStatusGatewayRequest);
	            DeviceResponsePayload device = deviceService.updateGatewayStatus(installationStatusGatewayRequest,jwtUser.getUsername());
	           
	         //   Logutils.logger(className,methodName, context.getLogUUId(), "Before calling updateGatewayStatus method from deviceService", context.getLogUUId(), InstallationStatusGatewayRequest.class);
	            return new ResponseEntity<ResponseBodyDTO<DeviceResponsePayload>>(
						new ResponseBodyDTO<DeviceResponsePayload>(true, "Fetched Device(s) Successfully",
								device),
						HttpStatus.OK);
	        } catch (Exception exception) {
	            logger.error("Exception while updating Gateway", exception);
	            return new ResponseEntity(
	                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }
	 
	 
	 //-----------------------------------Aamir 1 Start ------------------------------------//
	 
	 
	 @GetMapping("/sensor/can")
		public ResponseEntity<ResponseBodyDTO<List<DeviceResponsePayload>>> getSensorForCan(@RequestParam(name = "can") String can) {
			try {
				List<Device> deviceList = deviceService.getSensorsForCan(can);
				List<DeviceResponsePayload> deviceResponsePayload = new ArrayList<>();

				for (Device element : deviceList) {
					DeviceResponsePayload deviceResponsePayload1 = new DeviceResponsePayload();
					BeanUtils.copyProperties(element, deviceResponsePayload1);
					deviceResponsePayload.add(deviceResponsePayload1);
				}

				return new ResponseEntity<ResponseBodyDTO<List<DeviceResponsePayload>>>(
						new ResponseBodyDTO<List<DeviceResponsePayload>>(true, "Fetched Device(s) Successfully", deviceResponsePayload), HttpStatus.OK);
				// return new ResponseEntity<List<Device>>(sensorList, HttpStatus.OK);
			} catch (Exception e) {
				logger.error("Exception while getting sensors for can {}", can, e);
				return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	 
	 
	 @GetMapping("/gateway/uuid/{uuid}")
		public ResponseEntity<ResponseBodyDTO<Device>> getGatewayByUuid(@PathVariable("uuid") String uuid) {
			try {
				Device device = deviceService.getGatewayByUuid(uuid);
				return new ResponseEntity<ResponseBodyDTO<Device>>(new ResponseBodyDTO<Device>(true, "Fetched Device(s) Successfully", device), HttpStatus.OK);
			} catch (Exception exception) {
				logger.error("Exception while getting gateway for imei {}", uuid, exception);

				return new ResponseEntity(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	 
	 
	 @GetMapping("/can-status")
		public ResponseEntity<ResponseBodyDTO<List<Device>>> getGatewaysByAccountNumberAndStatus(@RequestParam(value = "logUuid", required = false) String logUuid,
				@RequestParam(value = "can") String accountNumber, @RequestParam(value = "status", required = false) String status) {
		 String methodName="getGatewaysByAccountNumberAndStatus";
			try {
				 Logutils.log(className,methodName,logUuid,"Before getting response from getGatewaysByAccountNumberAndStatus method from device controller ",logger);
				List<Device> device = deviceService.getGatewaysByAccountNumberAndStatus(accountNumber, status);
				 Logutils.log(className,methodName,logUuid,"After getting response from getGatewaysByAccountNumberAndStatus method from device controller ",logger);
				return new ResponseEntity<ResponseBodyDTO<List<Device>>>(new ResponseBodyDTO<List<Device>>(true, "Fetched Device(s) Successfully", device), HttpStatus.OK);
			} catch (Exception exception) {
				logger.error("Exception while fetching Device", exception);
				return new ResponseEntity(new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	 
	 
	 
	 @GetMapping("/product-name/{productName}")
		public ResponseEntity<ResponseBodyDTO<List<Attribute>>> getAttributeListByProductName(@Validated @PathVariable("productName") String producName) {
			String methodName="getAttributeListByProductName";
			Context context =new Context();
			String logUUid=context.getLogUUId();
			Logutils.log(className,methodName,logUUid,"Before getting response from getProductByName method from device controller ",logger);
			try {
				List<Attribute> attributeResponse = productMasterService.getProductByName(producName);
				if(attributeResponse!=null && attributeResponse.size()>0)
				{
					Logutils.log(className,methodName,logUUid,"  size from attributeResponse : " + attributeResponse.size(),logger);
				}
				Logutils.log(className,methodName,logUUid,"Exiting From Device Controller " ,logger);
				return new ResponseEntity<ResponseBodyDTO<List<Attribute>>>(new ResponseBodyDTO<List<Attribute>>(true, "Fetched Device(s) Successfully", attributeResponse),
						HttpStatus.OK);
			} catch (DeviceException exception) {
				logger.error("Exception occurred while getting attributeResponseList", exception);
				return new ResponseEntity(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			} catch (Exception exception) {
				logger.error("Exception occurred while getting attributeResponseList", exception);
				return new ResponseEntity(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
//	@GetMapping("/lookup")
//	public ResponseEntity<ResponseBodyDTO<String>> getLookupValue(@RequestParam(name = "field") String field) {
//		try {
//			String value = deviceService.getLookupValue(field);
//			return new ResponseEntity<ResponseBodyDTO<String>>(
//					new ResponseBodyDTO<String>(true, "Fetched Device(s) Successfully", value), HttpStatus.OK);
//		} catch (DeviceException exception) {
//			logger.error("Exception occurred while getting attributeResponseList", exception);
//			return new ResponseEntity(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//		} catch (Exception exception) {
//			logger.error("Exception occurred while getting attributeResponseList", exception);
//			return new ResponseEntity(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}
	
	 //-----------------------------------Aamir 1 End ------------------------------------//
	
	 @GetMapping("/gateway-pagination-new")
		public ResponseEntity<Page<Device>> getGatewaysByAccountNumberAndStatusWithPaginationNew(
				@RequestParam(value = "can", required = false) String accountNumber,
				@RequestParam(value = "status", required = false) String status,
				@RequestParam(value = "_page", required = true) Integer page,
				@RequestParam(value = "_limit", required = true) Integer pageSize,
				@RequestParam(value = "_sort", required = false) String sort,
				@RequestParam(value = "_order", required = false) String order,
				@RequestParam(value="cans", required = false) List<String>  cans,
				@RequestParam(value = "time_of_last_download", required = false) String timeOfLastDownload) {
			try {
				logger.info("gateway-pagination ============== sort is : " + sort + " order is : " + order + " status : " + status);
				if(sort == null || sort.equalsIgnoreCase("") || sort.equalsIgnoreCase("null")) {
					sort = null;
					order = null;
				} else if(order == null || order.equalsIgnoreCase("") || order.equalsIgnoreCase("null")) {
					sort = null;
					order = null;
				}
				
				Instant lastDownloadeTime = null;
		  		if(timeOfLastDownload != null && !timeOfLastDownload.isEmpty() && timeOfLastDownload != "") {
		  			lastDownloadeTime = Instant.ofEpochMilli(Long.parseLong(timeOfLastDownload));
		  			logger.info("Last Downloaded Time: " + lastDownloadeTime + " Account Number: "+ accountNumber);
		  		}
				logger.info("gateway-pagination ============== sort is : " + sort + " order is : " + order + " status : " + status);
				Page<Device> gatewayPage = deviceService.getGatewaysByAccountNumberAndStatusWithPaginationNew(accountNumber,
						status, getPageable(page - 1, pageSize, sort, order),cans, lastDownloadeTime);
				return new ResponseEntity(gatewayPage, HttpStatus.OK);

			} catch (Exception exception) {
				logger.error("Exception while fetching Gateways", exception);
				return new ResponseEntity(new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	 
	 
	 
	 @GetMapping("/gateway-pagination-v2")
		public ResponseEntity<Page<GatewayDetailsBean>> getGatewaysByAccountNumberAndStatusWithPaginationV2(
				@RequestParam(value = "can", required = false) String accountNumber,
				@RequestParam(value = "status", required = false) String status,
				@RequestParam(value = "_page", required = true) Integer page,
				@RequestParam(value = "_limit", required = true) Integer pageSize,
				@RequestParam(value = "_sort", required = false) String sort,
				@RequestParam(value = "_order", required = false) String order,
				@RequestParam(value="cans", required = false) List<String>  cans,
				@RequestParam(value = "time_of_last_download", required = false) String timeOfLastDownload) {
			try {
				String methodName="getGatewaysByAccountNumberAndStatusWithPaginationV2";
				Context context =new Context();
				String logUUid=context.getLogUUId();
				logger.info("gateway-pagination ============== sort is : " + sort + " order is : " + order + " status : " + status);
				if(sort == null || sort.equalsIgnoreCase("") || sort.equalsIgnoreCase("null")) {
					sort = null;
					order = null;
				} else if(order == null || order.equalsIgnoreCase("") || order.equalsIgnoreCase("null")) {
					sort = null;
					order = null;
				}
				
				Instant lastDownloadeTime = null;
		  		if(timeOfLastDownload != null && !timeOfLastDownload.isEmpty() && timeOfLastDownload != "") {
		  			lastDownloadeTime = Instant.ofEpochMilli(Long.parseLong(timeOfLastDownload));
		  			logger.info("Last Downloaded Time: " + lastDownloadeTime + " Account Number: "+ accountNumber);
		  		}
				logger.info("gateway-pagination ============== sort is : " + sort + " order is : " + order + " status : " + status);
				Logutils.log(className,methodName,logUUid,"Before getting response from getGatewaysByAccountNumberAndStatusWithPaginationV2 method from device controller ",logger);
				Page<GatewayDetailsBean> gatewayPage = deviceService.getGatewaysByAccountNumberAndStatusWithPaginationV2(accountNumber,
						status, getPageable(page - 1, pageSize, sort, order),cans, lastDownloadeTime);
				Logutils.log(className,methodName,logUUid,"After getting response from getGatewaysByAccountNumberAndStatusWithPaginationV2 method from device controller ",logger);
				return new ResponseEntity(gatewayPage, HttpStatus.OK);

			} catch (Exception exception) {
				logger.error("Exception while fetching Gateways", exception);
				return new ResponseEntity(new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	 
	
	 @PutMapping("/update-sensor")
	    public ResponseEntity<ResponseBodyDTO<Device>> updateSensorBySensorObj(@Valid @RequestBody Device sensor) {
	        try {
	        	String methodName="updateSensorBySensorObj";
	        	Context context=new Context();
				String logUUid=context.getLogUUId();
				Logutils.log(className,methodName,logUUid,"Inside updateSensorBySensorObj Method From Device Controller " ,logger);
		 Logutils.log(className,methodName,logUUid,"Before getting response from updateSensorBySensorObj method from device controller ",logger);
	        	Device sensorObj = deviceService.updateSensorBySensorObj(sensor);
	        	if(sensorObj!=null)
	        	{
	        		Logutils.log(className,methodName,logUUid,"  sensorObj : " + sensorObj.getUuid(),logger);
	        	}
	        	Logutils.log(className,methodName,logUUid,"Exiting From Device Controller " ,logger);
	            return new ResponseEntity<>(
	                    new ResponseBodyDTO<Device>(true, "Successfully updated Sensor",
	                    		sensorObj), HttpStatus.OK);
	        } catch (Exception exception) {
	            logger.error("Exception while updating Sensor", exception);
	            return new ResponseEntity(
	                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }

	@ApiOperation(value = "Batch Update device detail", notes = "API to Batch update device detail", response = String.class, tags = {
			"Device Management" })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Batch Updated asset & device details successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PutMapping("/batch-update-device")
	public ResponseEntity<ResponseDTO> batchUpdateDeviceDetails(
			@RequestBody BatchDeviceEditPayload batchDeviceEditPayload, HttpServletRequest httpServletRequest) {
		Boolean status = false;
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			logger.info("Request received to batch update device detail {}", batchDeviceEditPayload.toString());
			status = deviceService.batchUpdateAssetDeviceDetails(batchDeviceEditPayload, jwtUser.getUsername());
		} catch (Exception e) {
			logger.error("Exception occurred in batch updating of device detail", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Device details updated successfully"),
				HttpStatus.CREATED);
	}
	
	@ApiOperation(value = "Get device detail with forwarding and parent group", 
			notes = "API to get device detail with forwarding and parent group", 
			response = ResponseBodyDTO.class, 
			tags = { "Device Management" })
	@ApiResponses(value = { 
		@ApiResponse(code = 200, message = "Success", response = DeviceListDTO.class),
		@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/group-details")
	public ResponseEntity<ResponseBodyDTO<DeviceResponsePayload>> getDeviceWithForwardingAndParentGroup(
		@RequestParam(value = "imei") String imei) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info(
					"Before getting response from getDeviceWithForwardingAndParentGroup method from device controller :" + stopWatch.prettyPrint());
			DeviceResponsePayload deviceDetail = deviceService.getDeviceWithForwardingAndParentGroup(imei);
			stopWatch.stop();
			logger.info(
					"After getting response from getDeviceWithForwardingAndParentGroup method from device controller :" + stopWatch.prettyPrint());
			return new ResponseEntity<>(
					new ResponseBodyDTO<>(true, "Fetched device detail with forwarding and parent group successfully",
							deviceDetail),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting device  with forwarding and parent group", exception);
			return new ResponseEntity<>(new ResponseBodyDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@ApiOperation(value = "Get last device report", notes = "API to get last device report from Elastic", response = ResponseBodyDTO.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = DeviceReportDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/device-report")
	public ResponseEntity<ResponseBodyDTO<DeviceReportDTO>> getLastReport(@RequestParam("device_id") String deviceId)
			throws IOException {
		DeviceReportDTO deviceReport = deviceService.getLatestReportFromElastic(deviceId);
		return new ResponseEntity<>(
				new ResponseBodyDTO<>(true, "Fetched last device report successfully", deviceReport), HttpStatus.OK);
	}
	
	@ApiOperation(value = "Get device report by date range", notes = "API to get device report by date range from Elastic", response = ResponseBodyDTO.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = DeviceReportDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/device-reports/")
	public ResponseEntity<ResponseBodyDTO<List<DeviceReportDTO>>> getReportsByDateRange(
			@RequestParam("device_id") String deviceId, @RequestParam(required = false) Integer from,
			@RequestParam(required = false) Integer size, @RequestParam("from_date") String fromDate,
			@RequestParam("to_date") String toDate) throws IOException {
		List<DeviceReportDTO> deviceReports = deviceService.getReportsByDateRange(deviceId, from, size, fromDate, toDate);
		return new ResponseEntity<>(
				new ResponseBodyDTO<>(true, "Fetched device reports successfully", deviceReports), HttpStatus.OK);
	}

	@Operation(description = "API to get Device status from DB for device")
	@GetMapping("/device-status/{device_id}")
	public ResponseEntity<String> getDeviceStatusFromDB(@PathVariable("device_id") String deviceId)
			throws Exception {
		UUID uuid = UUID.randomUUID();
		String messageUUID = uuid.toString();
		String status = deviceService.getDeviceStatusFromDB(deviceId, messageUUID);
		if (!"ERROR".equalsIgnoreCase(status)) {
			return new ResponseEntity<>(status, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(status, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
