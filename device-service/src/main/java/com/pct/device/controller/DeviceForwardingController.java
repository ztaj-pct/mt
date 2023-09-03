package com.pct.device.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.pct.common.dto.ForwardRuleResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
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

import com.pct.common.dto.ResponseDTO;
import com.pct.common.payload.DeviceForwardingRequest;
import com.pct.common.util.JwtUser;
import com.pct.device.dto.DeviceIgnoreForwardingRuleDTO;
import com.pct.device.dto.ImportForwardingDTO;
import com.pct.device.dto.ImportForwardingResponseDTO;
import com.pct.device.dto.ResponseBodyDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.DeviceDataForwardingBulkUploadRequest;
import com.pct.device.payload.DeviceForwardingResponse;
import com.pct.device.payload.ProductMasterResponse;
import com.pct.device.service.DeviceForwardingService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/df")
public class DeviceForwardingController {
	
	@Autowired
	DeviceForwardingService deviceForwardingService;

	private static final Logger logger = LoggerFactory.getLogger(DeviceForwardingController.class);

	@ApiOperation(value = "Add DeviceForwarding", notes = "API For add a new DeviceForwarding", response = String.class, tags = {
			"DeviceForwarding Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/device-forwarding")
	public ResponseEntity<ResponseDTO> addDeviceForwarding(
			@Valid @RequestBody DeviceForwardingRequest deviceForwardingRequest) throws Exception {
		try {
			logger.info("inside add device forwarding:" + deviceForwardingRequest);
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if (deviceForwardingRequest != null) {
				deviceForwardingService.addDeviceForwarding(deviceForwardingRequest,jwtUser.getUsername());
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Failed while adding device forwarding ", e);
			throw new Exception("Failed while adding device forwarding");
		}
		return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "DeviceForwarding added successfully"),
				HttpStatus.CREATED);
	}

	@ApiOperation(value = "Update DeviceForwarding", notes = "API For update a DeviceForwarding", response = String.class, tags = {
			"DeviceForwarding Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PutMapping("/device_forwarding")
	public ResponseEntity<ResponseDTO> upDateDeviceForwarding(
			@Valid @RequestBody DeviceForwardingRequest deviceForwardingRequest) {
		try {
			System.out.println("deviceForwardingRequest:" + deviceForwardingRequest);
			if (deviceForwardingRequest != null) {
//				deviceForwardingService.updateDeviceForwarding(deviceForwardingRequest);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Failed while updating device forwarding ", e);
			throw new RuntimeException("Failed while updating device forwarding ");
		}
		return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "DeviceForwarding updated successfully"),
				HttpStatus.CREATED);
	}

	@ApiOperation(value = "Get deviceForwarding", notes = "API to get deviceForwarding", response = ProductMasterResponse.class, tags = {
			"DeviceForwarding Management" })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Fetched deviceForwarding List(s) successfully", response = ProductMasterResponse.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/device_forwarding/{uuid}")
	public ResponseEntity<ResponseBodyDTO<List<DeviceForwardingResponse>>> getDeviceForwarding(
			HttpServletRequest httpServletRequest, @PathVariable(value = "uuid", required = false) String uuid) {
		logger.info("Received request to fetch deviceForwarding list for uuid " + uuid);
		try {
			List<DeviceForwardingResponse> ProductMasterList = deviceForwardingService.getDeviceForwardingByUuid(uuid);
			return new ResponseEntity<ResponseBodyDTO<List<DeviceForwardingResponse>>>(
					new ResponseBodyDTO<List<DeviceForwardingResponse>>(true,
							"Fetched deviceForwarding List(s) successfully", ProductMasterList),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting deviceForwarding", exception);
			return new ResponseEntity<ResponseBodyDTO<List<DeviceForwardingResponse>>>(
					new ResponseBodyDTO<List<DeviceForwardingResponse>>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get AllDeviceForwardings", notes = "API to AllDeviceForwardings", response = ProductMasterResponse.class, tags = {
			"DeviceForwarding Management" })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Fetched deviceForwarding List(s) successfully", response = ProductMasterResponse.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })

	@GetMapping("/device_forwarding")
	public ResponseEntity<ResponseBodyDTO<List<DeviceForwardingResponse>>> getAllDeviceForwardings(
			HttpServletRequest httpServletRequest) {
		logger.info("inside getAllDeviceForwardings");
		try {
			List<DeviceForwardingResponse> ProductMasterList = deviceForwardingService.getAllDeviceForwarding();
			return new ResponseEntity<ResponseBodyDTO<List<DeviceForwardingResponse>>>(
					new ResponseBodyDTO<List<DeviceForwardingResponse>>(true,
							"Fetched deviceForwarding List(s) successfully", ProductMasterList),
					HttpStatus.OK);
		} catch (Exception exception) {
			exception.printStackTrace();
			logger.error("Exception occurred while getting deviceForwarding", exception);
			return new ResponseEntity<ResponseBodyDTO<List<DeviceForwardingResponse>>>(
					new ResponseBodyDTO<List<DeviceForwardingResponse>>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/device_forwarding_imei/{imei}")
	public ResponseEntity<ResponseBodyDTO<List<DeviceForwardingResponse>>> getAllDeviceForwardingsByImei(
			HttpServletRequest httpServletRequest, @PathVariable(value = "imei", required = false) String imei) {
		logger.info("inside getAllDeviceForwardings");
		try {
			List<DeviceForwardingResponse> ProductMasterList = deviceForwardingService
					.getAllDeviceForwardingByImei(imei);
			return new ResponseEntity<ResponseBodyDTO<List<DeviceForwardingResponse>>>(
					new ResponseBodyDTO<List<DeviceForwardingResponse>>(true,
							"Fetched deviceForwarding List(s) successfully", ProductMasterList),
					HttpStatus.OK);
		} catch (Exception exception) {
			exception.printStackTrace();
			logger.error("Exception occurred while getting deviceForwarding", exception);
			return new ResponseEntity<ResponseBodyDTO<List<DeviceForwardingResponse>>>(
					new ResponseBodyDTO<List<DeviceForwardingResponse>>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Delete DeviceForwarding", notes = "API to deleteDeviceForwarding", response = ProductMasterResponse.class, tags = {
			"DeviceForwarding Management" })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Delete deviceForwarding successfully", response = ProductMasterResponse.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@DeleteMapping("/device_forwarding/{uuid}")
	public ResponseEntity<ResponseDTO> deleteDeviceForwarding(HttpServletRequest httpServletRequest,
			@PathVariable(value = "uuid", required = false) String uuid) {
		logger.info("Received request to Delete deviceForwarding for uuid " + uuid);
		try {
			deviceForwardingService.deleteDeviceForwarding(uuid);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "DeviceForwarding deletion successfully"),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while deleting deviceForwarding", exception);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "DeviceForwarding deletion successfully"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@ApiOperation(value = "Get Device Forwarding Details From Redis", notes = "API to get device forwarding details from redis", response = ProductMasterResponse.class, tags = {
	"DeviceForwarding Management" })
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "Get device forwarding details successfully", response = ProductMasterResponse.class),
		@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/from/redis")
	public ResponseEntity<ResponseBodyDTO<Map<String, Object>>> getDeviceFrowardingDetailsFromRedis(
				@RequestParam(value = "imei",required = true) String imei
			) {
		logger.info("Received request to get device forwarding details from redis for imei " + imei);
		try {
			Map<String, Object> deviceforwardingMap = deviceForwardingService.getDeviceForwardingDetailsFromRedis(imei);	
			return new ResponseEntity<ResponseBodyDTO<Map<String, Object>>>(new ResponseBodyDTO(true, "DeviceForwarding deletion successfully", deviceforwardingMap),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while get device forwarding details from redis", exception);
			return new ResponseEntity<ResponseBodyDTO<Map<String, Object>>>(new ResponseBodyDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@ApiOperation(value = "Get device ignore rule imeis using customer account number", notes = "API to get device ignore rule imeis using customer account number", response = ProductMasterResponse.class, tags = {
	"DeviceForwarding Management" })
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "Get device ignore rule imeis successfully", response = ProductMasterResponse.class),
		@ApiResponse(code = 400, message = "Bad Request"), 
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/ignore/rules/imei")
	public ResponseEntity<ResponseBodyDTO<Map<String, Set<String>>>> getIgnoreRuleImeisByCustomerAccountNumber(
				@RequestParam(value = "customerAccountNumber",required = true) String customerAccountNumber
			) {
		logger.info("Received request to get ignore rules imeis using customer account number : " + customerAccountNumber);
		try {
			Map<String, Set<String>> resultMap = deviceForwardingService.getIgnoreRuleImeisByCustomerAccountNumber(customerAccountNumber);	
			return new ResponseEntity<>(new ResponseBodyDTO(true, "Get device ignore rule imeis successfully", resultMap),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while ignore rules imeis using customer account number ", exception);
			return new ResponseEntity<>(new ResponseBodyDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@ApiOperation(value = "Get device ignore rules using device imei", notes = "API to get device ignore rules using device imei", response = ProductMasterResponse.class, tags = {
	"DeviceForwarding Management" })
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "Get device ignore rules successfully", response = ProductMasterResponse.class),
		@ApiResponse(code = 400, message = "Bad Request"), 
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/ignore/rules")
	public ResponseEntity<ResponseBodyDTO<List<DeviceIgnoreForwardingRuleDTO>>> getIgnoreRulesUsingDeviceImei(
				@RequestParam(value = "imei", required = true) String imei
			) {
		logger.info("Received request to get ignore rules using device imei : " + imei);
		try {
			List<DeviceIgnoreForwardingRuleDTO> result = deviceForwardingService.getIgnoreRulesUsingDeviceImei(imei);	
			return new ResponseEntity<>(new ResponseBodyDTO(true, "Get device ignore rules using device imei is successfully", result),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while ignore rules s using device imei ", exception);
			return new ResponseEntity<>(new ResponseBodyDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@ApiOperation(value = "Bulk update Device data forwarding", notes = "API For Bulk update Device data forwarding", response = String.class, tags = {
			"DeviceForwarding Management" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Ok", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), 
			@ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PutMapping("/bulk/update")
	public ResponseEntity<ResponseDTO> bulkUpdate(
			@Valid @RequestBody DeviceDataForwardingBulkUploadRequest request) {
		try {
			deviceForwardingService.bulkUpdateDeviceForwarding(request);
			return new ResponseEntity<>(new ResponseDTO(true, "Bulk update device data forwarding successfully done"),
					HttpStatus.OK);
		} catch (DeviceException e) {
			return new ResponseEntity<>(new ResponseDTO(false, e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Failed while bulk update device data forwarding ", e);
			return new ResponseEntity<>(new ResponseDTO(false, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Bulk update Device data forwarding rules for devices", notes = "API For Bulk update Device data forwarding rules", response = String.class, tags = {
			"DeviceForwarding Management" })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Ok", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PutMapping("/batch/rules")
	public ResponseEntity<ResponseBodyDTO<List<ForwardRuleResponseDTO>>> processDeviceForwardingRuleForDevice(
			@Valid @RequestBody DeviceDataForwardingBulkUploadRequest request) {
		logger.info("inside updateDataForwardingRulesForDevices");
		List<ForwardRuleResponseDTO> ruleResponseDTOs = new ArrayList<>();
		String token = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
				.getHeader("Authorization");
		try {
			Map<String, List<ForwardRuleResponseDTO.DeviceFwdRulResp>> map = deviceForwardingService
					.processDeviceForwardingRuleForDevice(request,token);
			map.forEach((imei, deviceFwdRuleResponses) -> {
				ForwardRuleResponseDTO forwardRuleResponseDTO = new ForwardRuleResponseDTO();
				List<ForwardRuleResponseDTO.DeviceFwdRulResp> deviceFwdRulResps = new ArrayList<>();
				deviceFwdRuleResponses.forEach(deviceFwdRuleResponse -> {
					ForwardRuleResponseDTO.DeviceFwdRulResp deviceFwdRulResp = forwardRuleResponseDTO.new DeviceFwdRulResp();
					deviceFwdRulResp.setForwardingRuleUrlUuid(deviceFwdRuleResponse.getForwardingRuleUrlUuid());
					deviceFwdRulResp.setMessage(deviceFwdRuleResponse.getMessage());
					deviceFwdRulResp.setStatus(deviceFwdRuleResponse.isStatus());
					deviceFwdRulResps.add(deviceFwdRulResp);
				});
				forwardRuleResponseDTO.setImei(imei);
				forwardRuleResponseDTO.setDeviceFwdRulResps(deviceFwdRulResps);
				boolean anyMatch = deviceFwdRulResps.stream()
						.anyMatch(device -> !("added".equals(device.getMessage())));
				forwardRuleResponseDTO.setStatus(!anyMatch);
				ruleResponseDTOs.add(forwardRuleResponseDTO);
			});
			return new ResponseEntity<>(new ResponseBodyDTO<>(true,
					"Bulk update device data forwarding rules successfully done", ruleResponseDTOs), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Failed while bulk update device data forwarding ", e);
			return new ResponseEntity<>(new ResponseBodyDTO<>(false, e.getMessage(), ruleResponseDTOs),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@ApiOperation(value = "Import device forwarding rules", notes = "API For import device forwarding rules", response = String.class, tags = {
			"DeviceForwarding Management" })
	@ApiResponses(value = { 
			@ApiResponse(code = 201, message = "imported", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/import/rules")
	public ResponseEntity<ResponseBodyDTO<List<ImportForwardingResponseDTO>>> importRules(
			@Valid @RequestBody List<ImportForwardingDTO> importForwardingDTOs,
			@RequestParam(name = "type", required = false) String type) {
		try {
			return new ResponseEntity<>(
					new ResponseBodyDTO<>(true, "Import device data forwarding rules successfully done",
							deviceForwardingService.importDeviceForwardingRules(importForwardingDTOs, type)),
					HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Failed while import device forwarding rules", e);
			return new ResponseEntity<>(new ResponseBodyDTO<>(false, e.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
