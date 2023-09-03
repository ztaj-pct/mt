package com.pct.device.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.GatewayType;
import com.pct.common.constant.IOTType;
import com.pct.common.controller.IApplicationController;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.model.Device;
import com.pct.common.util.JwtUser;
import com.pct.device.dto.GatewayCheckDataDTO;
import com.pct.device.dto.GatewayUpdateDTO;
import com.pct.device.dto.MessageDTO;
import com.pct.device.dto.SensorSectionDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.AssetsStatusPayload;
import com.pct.device.payload.GatewayBeanForMobileApp;
import com.pct.device.payload.GatewayBulkUploadRequestWithDeviceForwarding;
import com.pct.device.payload.GatewayDataCheckRequest;
import com.pct.device.payload.GatewayDetailPayLoad;
import com.pct.device.payload.GatewayPayload;
import com.pct.device.payload.GatewaySensorAssociation;
import com.pct.device.payload.GatewaySensorPayload;
import com.pct.device.payload.GatewaySummaryPayload;
import com.pct.device.payload.GatewayUploadRequest;
import com.pct.device.payload.UpdateGatewayRequest;
import com.pct.device.service.impl.GatewayServiceImpl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/gateway")
@Api(value = "/gateway", tags = "Gateway Management")
public class GatewayController implements IApplicationController<Device> {
	// @Autowired
	// private JwtUtil jwtUtil;

	@Autowired
	private GatewayServiceImpl gatewayService;

	Logger logger = LoggerFactory.getLogger(GatewayController.class);

	@ApiOperation(value = "Get gateway details", notes = "API to get gateway details", response = GatewayPayload.class, tags = {
			"Gateway Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get it", response = GatewayPayload.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping()
	public ResponseEntity<ResponseBodyDTO<List<GatewayPayload>>> getGateway(HttpServletRequest httpServletRequest,
			@RequestParam(value = "can", required = false) String accountNumber,
			@RequestParam(value = "device-uuid", required = false) String gatewayUuid,
			@RequestParam(value = "gateway-id", required = false) String gatewayId) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Before getting response from getGateway method from gateway controller :"
					+ stopWatch.prettyPrint());
			List<GatewayPayload> gatewayDetails = gatewayService.getGatewayDetails(accountNumber, gatewayUuid,
					gatewayId);
			stopWatch.stop();
			logger.info("After getting response from getGateway method from gateway controller :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity<ResponseBodyDTO<List<GatewayPayload>>>(
					new ResponseBodyDTO<List<GatewayPayload>>(true, "Fetched Gateway(s) Successfully", gatewayDetails),
					HttpStatus.OK);
		} catch (DeviceException exception) {
			logger.error("Exception occurred while getting gateway(s)", exception);
			return new ResponseEntity<>(new ResponseBodyDTO<>(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get gateway with pagination", notes = "API to gateway with pagination", response = Object.class, tags = {
			"Gateway Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get with pagination", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/page")
	public ResponseEntity<Object> getGatewayListWithPagination(
			@RequestParam(value = "can", required = true) String accountNumber,
			@RequestParam(value = "imei", required = false) String imei,
			@RequestParam(value = "device-uuid", required = false) String deviceUuid,
			@RequestParam(value = "device-status", required = false) DeviceStatus deviceStatus,
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "macAddress", required = false) String macAddress,
			@RequestParam(value = "time_of_last_download", required = false) String timeOfLastDownload,
			@RequestBody Map<String, String> filterValues, HttpServletRequest httpServletRequest) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("before getting response from getGatewayListWithPagination method from gateway controller :"
					+ stopWatch.prettyPrint());
			MessageDTO<Page<GatewayPayload>> messageDto = new MessageDTO<>("Fetched Gateway(s) Successfully", true);
			Page<GatewayPayload> device = gatewayService.getGatewayWithPagination(accountNumber, imei, deviceUuid,
					deviceStatus, filterValues, macAddress, getPageable(page - 1, size, sort, order), timeOfLastDownload);
			messageDto.setBody(device);
			logger.info("Inside getGatewayList Post Size ==== " + device.getTotalElements());
			messageDto.setTotalKey(device.getTotalElements());
			logger.info("Total Elements" + device.getTotalElements());

			messageDto.setCurrentPage(device.getNumber());
			logger.info("Current Page " + device.getNumber());

			messageDto.setTotal_pages(device.getTotalPages());
			logger.info("Total pages" + device.getTotalPages());
			stopWatch.stop();
			logger.info("After getting response from getGatewayListWithPagination method from gateway controller :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity<>(messageDto, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting gateway(s)", exception);
			return new ResponseEntity<>(new ResponseBodyDTO<>(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Add gateway detail", notes = "API to add gateway detail", response = String.class, tags = {
			"Gateway Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Added Successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping()
	public ResponseEntity<ResponseDTO> addGatewayDetail(@RequestBody GatewayUploadRequest gatewayUploadRequest,
			HttpServletRequest httpServletRequest) {
		Boolean status = false;
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			logger.info("Request received to add gateway detail {}", gatewayUploadRequest.toString());
			status = gatewayService.addGatewayDetail(gatewayUploadRequest, jwtUser.getUsername());
		} catch (Exception e) {
			logger.error("Exception occurred in adding of gateway detail", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Gateway details saved successfully"),
				HttpStatus.CREATED);
	}

	@ApiOperation(value = "Update Gateway detail", notes = "API to update  gateway detail", response = String.class, tags = {
			"Gateway Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Updated gateway Successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PutMapping()
	public ResponseEntity<ResponseDTO> updateGatewayDetail(@RequestBody GatewayDetailPayLoad gatewayDetailPayload,
			HttpServletRequest httpServletRequest) {
		Boolean status = false;
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			logger.info("Request received to update gateway detail {}", gatewayDetailPayload.toString());
			status = gatewayService.updateGatewayDetail(gatewayDetailPayload, jwtUser.getUsername());
		} catch (Exception e) {
			logger.error("Exception occurred in updating of gateway detail", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Gateway details updated successfully"),
				HttpStatus.CREATED);
	}

	@ApiOperation(value = "Delete the gateway details", notes = "API to delete the gateway details", response = String.class, tags = {
			"Gateway Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Success", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@DeleteMapping()
	public ResponseEntity<ResponseDTO> deleteGatewayDetail(@RequestParam(value = "_can", required = false) String can,
			@RequestParam(value = "_imei", required = false) String imei,
			@RequestParam(value = "_uuid", required = false) String uuid) {
		Boolean status = false;
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Request received to delete gateway detail {}" + stopWatch.prettyPrint());
			status = gatewayService.deleteGatewayDetail(can, imei, uuid);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Gateway deleted successfully"),
					HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Gateway Sensor Association", notes = "API to associate sensors with gateway ", response = String.class, tags = {
			"Gateway Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Added Successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/gateway-sensor")
	public ResponseEntity<ResponseDTO> gatewaySensorAssociation(
			@RequestBody GatewaySensorAssociation gatewaysensorRequest, HttpServletRequest httpServletRequest) {
		Boolean status = false;
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			logger.info("Request received to associate detail {}", gatewaysensorRequest.toString());
			status = gatewayService.associateGatewaySensor(gatewaysensorRequest, jwtUser.getUsername());
		} catch (Exception e) {
			logger.error("Exception occurred while associating detail", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Gateway sensor association saved successfully"),
				HttpStatus.CREATED);
	}

	@ApiOperation(value = "Get gateway & sensor details", notes = "API to get a gateway & sensor details", response = GatewayPayload.class, tags = {
			"Gateway Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get it", response = GatewayPayload.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/getConfiguration")
	public ResponseEntity<ResponseBodyDTO<List<GatewaySensorPayload>>> getConfigurationDetails(
			HttpServletRequest httpServletRequest, @RequestParam(value = "imei", required = false) String imei,
			@RequestParam(value = "device-uuid", required = false) String deviceUuid) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Before setting response from getConfigurationDetails method from gateway controller :"
					+ stopWatch.prettyPrint());
			List<GatewaySensorPayload> gatewayDetails = gatewayService.getGatewaySensorDetails(imei, deviceUuid);
			stopWatch.stop();
			logger.info("After getting response from getConfigurationDetails method from gateway controller :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity<ResponseBodyDTO<List<GatewaySensorPayload>>>(
					new ResponseBodyDTO<List<GatewaySensorPayload>>(true, "Fetched configuration details successfully",
							gatewayDetails),
					HttpStatus.OK);
		} catch (DeviceException exception) {
			logger.error("Exception occurred while getting gateway(s)", exception);
			return new ResponseEntity<ResponseBodyDTO<List<GatewaySensorPayload>>>(
					new ResponseBodyDTO<List<GatewaySensorPayload>>(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get gateway & sensor & assets details", notes = "API to get a gateway & sensor & assets details", response = GatewayPayload.class, tags = {
			"Gateway Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get it", response = GatewayPayload.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/assetsDetails")
	public ResponseEntity<ResponseBodyDTO<List<AssetsStatusPayload>>> getAssetsDetails(
			HttpServletRequest httpServletRequest, @RequestParam(value = "imei", required = false) String imei,
			@RequestParam(value = "device-uuid", required = false) String deviceUuid) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			logger.info("Before setting response from getConfigurationDetails method from gateway controller :"
					+ stopWatch.prettyPrint());
			List<AssetsStatusPayload> gatewayDetails = gatewayService.getAssetsStatusDetails(imei, deviceUuid,jwtUser.getUsername());
			stopWatch.stop();
			logger.info("After getting response from getAssetsDetails method from gateway controller :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity<ResponseBodyDTO<List<AssetsStatusPayload>>>(
					new ResponseBodyDTO<List<AssetsStatusPayload>>(true, "Fetched configuration details successfully",
							gatewayDetails),
					HttpStatus.OK);
		} catch (DeviceException exception) {
			logger.error("Exception occurred while getting gateway(s)", exception);
			return new ResponseEntity<ResponseBodyDTO<List<AssetsStatusPayload>>>(
					new ResponseBodyDTO<List<AssetsStatusPayload>>(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

//	@PostMapping("/bulk")
//	public ResponseEntity<MessageDTO<Map<String, List<String>>>> addBulkGatewayData(
//			@RequestBody GatewayBulkUploadRequest gatewayBulkUploadRequest, HttpServletRequest httpServletRequest) {
//		Boolean status = false;
//		try {
//			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//			logger.info("Username : " + jwtUser.getUsername());
//			logger.info("Request received to associate detail {}", gatewayBulkUploadRequest.toString());
//			status = gatewayService.uploadBulkGateway(gatewayBulkUploadRequest, jwtUser.getUsername());
//		} catch (Exception e) {
//			logger.error("Exception occurred in processing bulk adding of gateway", e);
//			return new ResponseEntity<MessageDTO<Map<String, List<String>>>>(
//					new MessageDTO<Map<String, List<String>>>(e.getMessage(), null, status),
//					HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//
//		return new ResponseEntity<MessageDTO<Map<String, List<String>>>>(
//				new MessageDTO<Map<String, List<String>>>("All Device saved successfully", null, status),
//				HttpStatus.CREATED);
//	}

	@PostMapping("/checkGatewayListData")
	public ResponseEntity<ResponseBodyDTO<GatewayCheckDataDTO>> checkGatewayListData(
			@RequestBody GatewayDataCheckRequest gatewayDataCheckRequest) {
		try {
			logger.info("Request bulkDeleteForGateways");
			GatewayCheckDataDTO gatewayCheckDataDTO = gatewayService.checkGatewayListData(gatewayDataCheckRequest);
			return new ResponseEntity<ResponseBodyDTO<GatewayCheckDataDTO>>(new ResponseBodyDTO<GatewayCheckDataDTO>(
					true, "Fetched Gateway And Sensors List Successfully", gatewayCheckDataDTO), HttpStatus.OK);
		} catch (DeviceException e) {
			logger.error("Exception while checking gateways", e);
			return new ResponseEntity<ResponseBodyDTO<GatewayCheckDataDTO>>(
					new ResponseBodyDTO<GatewayCheckDataDTO>(false, e.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception while saving shipment details.", e);
			return new ResponseEntity<ResponseBodyDTO<GatewayCheckDataDTO>>(
					new ResponseBodyDTO<GatewayCheckDataDTO>(false, e.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/bulkDelete")
	public ResponseEntity<ResponseDTO> bulkDeleteForGateways(@RequestBody List<String> listOfUuid) {
		try {
			logger.info("Request bulkDeleteForGateways");
			boolean status = gatewayService.bulkDeleteForGateways(listOfUuid);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Device(s) deleted successfully"),
					HttpStatus.OK);
		} catch (DeviceException e) {
			logger.error("Exception while bulk delete for gateways", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			logger.error("Exception while saving shipment details.", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, "Exception while bulk delete for gateways"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/bulkUpdate")
	public ResponseEntity<Object> bulkUpdate(@Validated @RequestBody UpdateGatewayRequest updateGatewayRequest,
			HttpServletRequest httpServletRequest) {
		logger.info("Inside updateAsset");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.info("Username : " + jwtUser.getUsername());
		Boolean status = false;
		try {
			status = gatewayService.updateGateway(updateGatewayRequest, jwtUser.getUsername());
		} catch (Exception e) {
			return new ResponseEntity<Object>(new MessageDTO<Object>(e.getMessage(), false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Object>(
				new MessageDTO<Object>("The selected gateway(s) have been updated with the new configuration.", status),
				HttpStatus.ACCEPTED);
	}

	@GetMapping("/sensor-section-list")
	public ResponseEntity<ResponseBodyDTO<List<SensorSectionDTO>>> getSensorList(
			HttpServletRequest httpServletRequest) {
		try {
			List<SensorSectionDTO> sensorList = gatewayService.getSensorSectionList();

			return new ResponseEntity<ResponseBodyDTO<List<SensorSectionDTO>>>(
					new ResponseBodyDTO<List<SensorSectionDTO>>(true, "Fetched Sensor Section List(s) Successfully",
							sensorList),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting sensor section(s)", exception);
			return new ResponseEntity<ResponseBodyDTO<List<SensorSectionDTO>>>(
					new ResponseBodyDTO<List<SensorSectionDTO>>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	@PostMapping("/gatewaysummary")
	public ResponseEntity<MessageDTO<Page<GatewaySummaryPayload>>> getCustomerGateways(
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_type", required = false) String type,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestBody Map<String, String> filterValues, HttpServletRequest httpServletRequest,
			HttpServletResponse response) {

		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.info("Username : " + jwtUser.getUsername());
		MessageDTO<Page<GatewaySummaryPayload>> messageDto = new MessageDTO<>(" Gateway Summary Feteched Successfully",
				true);
		Page<GatewaySummaryPayload> gateways = gatewayService.getCustomerGatewaySummary(
				getPageable(page - 1, size, sort, order), jwtUser.getUsername(), filterValues);
		messageDto.setBody(gateways);
		messageDto.setTotalKey(gateways.getTotalElements());
		messageDto.setCurrentPage(gateways.getNumber());
		messageDto.setTotal_pages(gateways.getTotalPages());
		return new ResponseEntity<MessageDTO<Page<GatewaySummaryPayload>>>(messageDto, HttpStatus.OK);
	}
	
	@PostMapping("/bulk")
	public ResponseEntity<MessageDTO<Map<String, List<String>>>> addBulkGatewayDataWithDeviceForwading(
			@RequestBody GatewayBulkUploadRequestWithDeviceForwarding gatewayBulkUploadRequestWithDeviceForwarding, HttpServletRequest httpServletRequest) {
		Boolean status = false;
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			logger.info("Request received to associate detail {}", gatewayBulkUploadRequestWithDeviceForwarding.toString());
			status = gatewayService.uploadBulkGatewayWithDeviceForwarding(gatewayBulkUploadRequestWithDeviceForwarding, jwtUser.getUsername());
		} catch (DeviceException e) {
			logger.error("Exception occurred in processing bulk adding of gateway", e);
			return new ResponseEntity<MessageDTO<Map<String, List<String>>>>(
					new MessageDTO<Map<String, List<String>>>(e.getMessage(), null, status),
					HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Exception occurred in processing bulk adding of gateway", e);
			return new ResponseEntity<MessageDTO<Map<String, List<String>>>>(
					new MessageDTO<Map<String, List<String>>>(e.getMessage(), null, status),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<MessageDTO<Map<String, List<String>>>>(
				new MessageDTO<Map<String, List<String>>>("All Device saved successfully", null, status),
				HttpStatus.CREATED);
	}

	@PostMapping("/exportGatewaySummary")
	public ResponseEntity<MessageDTO<Page<GatewaySummaryPayload>>> exportCustomerGatewaysToCsv(
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_type", required = false) String type,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestBody Map<String, String> filterValues, HttpServletRequest httpServletRequest,
			HttpServletResponse response) throws IOException {

		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.info("Username : " + jwtUser.getUsername());
		MessageDTO<Page<GatewaySummaryPayload>> messageDto = new MessageDTO<>(" Gateway Summary Feteched Successfully",
				true);
		Page<GatewaySummaryPayload> gateways = gatewayService.getCustomerGatewaySummary(
				getPageable(page - 1, size, sort, order), jwtUser.getUsername(), filterValues);
		
		gatewayService.getDeviceDetailsForCsv(gateways,	response);
				messageDto.setBody(gateways);
		messageDto.setTotalKey(gateways.getTotalElements());
		messageDto.setCurrentPage(gateways.getNumber());
		messageDto.setTotal_pages(gateways.getTotalPages());
		return new ResponseEntity<MessageDTO<Page<GatewaySummaryPayload>>>(messageDto, HttpStatus.OK);
	}
	
	@PutMapping("/bulk")
	public ResponseEntity<MessageDTO<List<GatewayUpdateDTO>>> uploadBulkGateway(
			@RequestBody GatewayBulkUploadRequestWithDeviceForwarding gatewayBulkUploadRequestWithDeviceForwarding, HttpServletRequest httpServletRequest) {
		List<GatewayUpdateDTO> updateGatway;
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			logger.info("Request received to associate detail {}", gatewayBulkUploadRequestWithDeviceForwarding.toString());
			updateGatway = gatewayService.uploadBulkGateway(gatewayBulkUploadRequestWithDeviceForwarding, jwtUser.getUsername());
		} catch (DeviceException e) {
			logger.error("Exception occurred in processing bulk adding of gateway", e);
			return new ResponseEntity<MessageDTO<List<GatewayUpdateDTO>>>(
					new MessageDTO<List<GatewayUpdateDTO>>(e.getMessage(), null, false),
					HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Exception occurred in processing bulk adding of gateway", e);
			return new ResponseEntity<MessageDTO<List<GatewayUpdateDTO>>>(
					new MessageDTO<List<GatewayUpdateDTO>>(e.getMessage(), null, false),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<MessageDTO<List<GatewayUpdateDTO>>>(
				new MessageDTO<List<GatewayUpdateDTO>>("All Device updated successfully", updateGatway, true),
				HttpStatus.OK);
	}
	
	
	@GetMapping("/paginated-api-for-mobile-app")
	public ResponseEntity<Object> getGatewayWithPagination(HttpServletRequest httpServletRequest,
			@RequestParam(value = "can", required = false) String accountNumber,
			@RequestParam(value = "imei", required = false) String imei,
			@RequestParam(value = "gateway-uuid", required = false) String gatewayUuid,
			@RequestParam(value = "gateway-status", required = false) DeviceStatus gatewayStatus,
			@RequestParam(value = "type", required = false) IOTType type,
			@RequestParam(value = "macAddress", required = false) String macAddress,
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "time_of_last_download", required = false) String timeOfLastDownload) {
		try {
			StopWatch stopWatch  = new StopWatch();
      		stopWatch.start();
      		logger.info("before getting response from getGateway method from gateway controller method :" + stopWatch.prettyPrint());
      		MessageDTO<Page<GatewayBeanForMobileApp>> messageDto = new MessageDTO<>("Fetched Gateway(s) Successfully", true);
			Page<GatewayBeanForMobileApp> gatewayBeanList = gatewayService.getGatewayWithPagination(accountNumber, imei, gatewayUuid, gatewayStatus, type,
					macAddress, getPageable(page - 1, size, sort, order), messageDto, timeOfLastDownload);
			
			stopWatch.stop();
            logger.info("after getting response from getGateway method from gateway controller method :" + stopWatch.prettyPrint());
            return new ResponseEntity(messageDto, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting gateway(s)", exception);
			return new ResponseEntity<Object>(
					new ResponseBodyDTO<GatewayBeanForMobileApp>(false, exception.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	
	
}
