package com.pct.device.command.controller;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.model.User;
import com.pct.common.payload.DeviceATCommandReqResponse;
import com.pct.common.util.JwtUser;
import com.pct.common.util.Logutils;
import com.pct.device.command.dto.DeviceCommandListDTO;
import com.pct.device.command.dto.DeviceCommandStatusResponse;
import com.pct.device.command.dto.MessageDTO;
import com.pct.device.command.dto.ResponseBodyDTO;
import com.pct.device.command.entity.DeviceCommand;
import com.pct.device.command.exception.DeviceCommandException;
import com.pct.device.command.payload.DeviceCommandBean;
import com.pct.device.command.payload.DeviceCommandQueue;
import com.pct.device.command.payload.DeviceCommandRequest;
import com.pct.device.command.payload.DeviceCommandRequestPayload;
import com.pct.device.command.payload.DeviceCommandResponse;
import com.pct.device.command.payload.DeviceResponse;
import com.pct.device.command.payload.GatewayRequestPayload;
import com.pct.device.command.payload.RedisDeviceCommand;
import com.pct.device.command.service.impl.DeviceCommonServiceImpl;
import com.pct.device.command.util.Constants;
import com.pct.device.command.util.RestUtils;
import com.pct.device.command.util.ValidationUtils;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/gateway-command")
public class GatewayCommandController implements IApplicationController<DeviceCommand> {

	private static final Logger logger = LoggerFactory.getLogger(GatewayCommandController.class);

	@Autowired
	private DeviceCommonServiceImpl deviceCommandService;

//	@Autowired
//	private RestUtils restUtils;

	@Operation(description = "API to send the ATC Request to gateway")
	@PostMapping("/atc-request")
	public ResponseEntity<ResponseBodyDTO> sendAtRequest(
			@Validated @RequestBody GatewayRequestPayload gatewayRequestPayload,
			HttpServletRequest httpServletRequest) {
		UUID uuid = UUID.randomUUID();
		String messageUUID = uuid.toString();
		logger.info("Inside addDeviceCommand Api Controller MessageUUID : " + messageUUID + " Device Id : "
				+ gatewayRequestPayload.getGatewayId());
		try {
			logger.info("before sendOnDemandCommand method call from service");
			DeviceCommandRequest deviceCommandRequest = new DeviceCommandRequest();
			deviceCommandRequest.setAtCommand(gatewayRequestPayload.getAtCommand());
			deviceCommandRequest.setDeviceId(gatewayRequestPayload.getGatewayId());
			deviceCommandRequest.setPriority(gatewayRequestPayload.getPriority() + "");
			deviceCommandRequest.setSource(gatewayRequestPayload.getSource());
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			// User user = restUtils.getUserFromAuthService(jwtUser.getUsername());
			User user = new User();
			user.setUserName(jwtUser.getUsername());
			logger.info("Username After getting user object: " + user.getUserName());
			java.sql.Timestamp commandSentTimestamp = new java.sql.Timestamp(new java.util.Date().getTime());
			String formattedCommandSentTimestamp = String.format("%0" + Constants.TIMESTAMP_BYTES_LEN + "d",
					commandSentTimestamp.getTime());

			DeviceATCommandReqResponse deviceCommand = deviceCommandService.sendOnDemandCommand(deviceCommandRequest,
					messageUUID, user, formattedCommandSentTimestamp, commandSentTimestamp, false);
			if (deviceCommand != null) {
				deviceCommandService.sendATCommand(deviceCommandRequest.getDeviceId(),
						deviceCommandRequest.getAtCommand(), messageUUID, true, formattedCommandSentTimestamp);
			}
			logger.info("after sending OnDemand Command sendOnDemandCommand method call from service  MessageUUID : "
					+ messageUUID + " Device Id : " + gatewayRequestPayload.getGatewayId());
			return new ResponseEntity<>(new ResponseBodyDTO<>(true, "ATC Request sent successfully", deviceCommand),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter sw = Logutils.exception(e);
			logger.info(sw.toString());
			return new ResponseEntity<>(new ResponseBodyDTO<>(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}

//	@Operation(description = "API to get the queued ATC Request")
//	@GetMapping("/queued-atc-request")
//	public ResponseEntity<ResponseBodyDTO> getQueuedAtRequest(@RequestParam("gateway_Id") String deviceID,
//			HttpServletRequest httpServletRequest) {
//		logger.info("Inside getQueuedAtRequest method in Controller having deviceId = " + deviceID);
//		DeviceCommandQueue redisDeviceCommandList = null;
//		try {
//			logger.info("before calling getDeviceCommandListFromRedisObjects method from devicecommand service");
//			redisDeviceCommandList = deviceCommandService.getDeviceCommandListFromRedisObjects(deviceID);
//			logger.info("after calling getDeviceCommandListFromRedisObjects method from devicecommand service");
//			return new ResponseEntity(
//					new ResponseBodyDTO(true, "Queued ATC Request Fateched successfully", redisDeviceCommandList),
//					HttpStatus.OK);
//		} catch (Exception e) {
//			StringWriter sw = Logutils.exception(e);
//			logger.info(sw.toString());
//
//			e.printStackTrace();
//			return new ResponseEntity(new ResponseBodyDTO(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
//
//		}
//	}

	@ApiOperation(value = "Get ATCommand Queue Details", notes = "API to get ATCommand Queue Details", response = ResponseBodyDTO.class, tags = {
			"ATCommand Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get it", response = ResponseBodyDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/queued-atc-request")
	public ResponseEntity<ResponseBodyDTO> getQueuedATCRequest(HttpServletRequest httpServletRequest,
			@RequestParam(value = "device_id", required = true) String deviceId,
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order) {
		try {
			logger.info("Before getting response from getQueuedATCRequest method from atcommand controller method :");
			if (page == null) {
				page = 1;
			} else if (page == -1) {
				throw new Exception("invalid.page_index.in.input");
			}

			if (size == null) {
				size = 25;
			}

			DeviceCommandQueue redisDeviceCommandList = null;
			List<RedisDeviceCommand> redisDeviceCommand = new ArrayList<>();
			try {
				logger.info("before calling getDeviceCommandListFromRedisObjects method from devicecommand service");
				redisDeviceCommandList = deviceCommandService.getDeviceCommandListFromRedisObjects(deviceId);
				if (redisDeviceCommandList != null && redisDeviceCommandList.getRedisDeviceCommand() != null) {
					redisDeviceCommand = redisDeviceCommandList.getRedisDeviceCommand();
				}
			} catch (Exception e) {
				StringWriter sw = Logutils.exception(e);
				logger.info(sw.toString());
				e.printStackTrace();
				return new ResponseEntity<>(new ResponseBodyDTO<>(false, e.getMessage()),
						HttpStatus.INTERNAL_SERVER_ERROR);

			}
//			List<GatewayCommandResponse> atcReqQueue = atcommandService.getQueuedATCReq(deviceId);
			Pageable pageable = getPageable(page - 1, size, sort, order);
			int start = (int) pageable.getOffset();
			int end = Math.min((start + pageable.getPageSize()), redisDeviceCommand.size());
			Page<RedisDeviceCommand> page1 = new PageImpl<>(redisDeviceCommand.subList(start, end), pageable,
					redisDeviceCommand.size());
			MessageDTO<Page<RedisDeviceCommand>> messageDto = new MessageDTO<>(
					"Fetched Queued At Request(s) Successfully", true);
			messageDto.setBody(page1);
			messageDto.setTotalKey(page1.getTotalElements());
			return new ResponseEntity(messageDto, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Queued ATC(s)", exception);
			exception.printStackTrace();
			return new ResponseEntity(new ResponseBodyDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(description = "API to send ATC Command Response")
	@PostMapping("/atc-response")
	public ResponseEntity<ResponseBodyDTO> sendATResponseCommand(
			@Validated @RequestBody GatewayRequestPayload deviceCommandRequest, HttpServletRequest httpServletRequest) {
		logger.info("Inside addDeviceCommand Api Controller");
		DeviceResponse deviceResponse = new DeviceResponse();
		try {
			deviceResponse.setDeviceID(deviceCommandRequest.getGatewayId());
			deviceResponse.setCommandSent(deviceCommandRequest.getAtCommand());
			String command = deviceCommandService.atCommandResponseProcessor(deviceResponse);
			return new ResponseEntity<>(new ResponseBodyDTO<>(true, "ATC Response sent successfully", command),
					HttpStatus.OK);
		} catch (Exception e) {
			StringWriter sw = Logutils.exception(e);
			logger.info(sw.toString());

			e.printStackTrace();
			return new ResponseEntity<>(new ResponseBodyDTO<>(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	@Operation(description = "API to send ATC Command Response")
	@PostMapping("/atc-delete")
	public ResponseEntity<ResponseBodyDTO> deleteAtCommand(
			@Validated @RequestBody GatewayRequestPayload deviceCommandRequest, HttpServletRequest httpServletRequest) {
		logger.info("Inside addDeviceCommand Api Controller");
		try {
			String command = deviceCommandService.deleteAtCommand(deviceCommandRequest);
			return new ResponseEntity<>(new ResponseBodyDTO<>(true, "Deleted Successfully", command), HttpStatus.OK);
		} catch (Exception e) {
			StringWriter sw = Logutils.exception(e);
			logger.info(sw.toString());
			e.printStackTrace();
			return new ResponseEntity<>(new ResponseBodyDTO<>(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	@Operation(description = "API to send ATC Command Response")
	@PostMapping("/atc-delete-all")
	public ResponseEntity<ResponseBodyDTO> deleteAllAtCommand(
			@Validated @RequestBody GatewayRequestPayload deviceCommandRequest, HttpServletRequest httpServletRequest) {
		logger.info("Inside addDeviceCommand Api Controller");

		try {
			if (deviceCommandRequest.getDeviceId() != null) {
				deviceCommandRequest.setGatewayId(deviceCommandRequest.getDeviceId());
			}
			
			String command = deviceCommandService.deleteAllAtCommand(deviceCommandRequest);
			return new ResponseEntity<>(new ResponseBodyDTO<>(true, "Deleted Successfully", command), HttpStatus.OK);
		} catch (Exception e) {
			StringWriter sw = Logutils.exception(e);
			logger.info(sw.toString());

			e.printStackTrace();
			return new ResponseEntity<>(new ResponseBodyDTO<>(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	@Operation(description = "API to get All the Gateway ATC Commands curresponding to gatewayId")

	@GetMapping("/gateway-atc-request")
	public ResponseEntity<Object> getGatewayAtRequest(HttpServletRequest httpServletRequest,

			@RequestParam(value = "gateway_Id", required = false) String deviceId,
			@RequestParam(value = "device_id", required = false) String deviceId1,
			
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,

			@RequestParam(value = "limit", required = false, defaultValue = "1") Integer size,

			@RequestParam(value = "sort", required = false) String sort,

			@RequestParam(value = "order", required = false) String order) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info(
					"before getting response from getAllDeviceCommands method from DeviceCommand controller method :"
							+ stopWatch.prettyPrint());
			MessageDTO<Page<DeviceCommandBean>> messageDto = new MessageDTO<>("Fetched GatewayCommands(s) Successfully",
					true);
			
			if (deviceId1 != null) {
				deviceId = deviceId1;
			}
			Page<DeviceCommandBean> deviceCommandBeanList = deviceCommandService
					.getDeviceCommandWithPagination(deviceId, getPageable(page - 1, size, sort, "asc"));
			messageDto.setBody(deviceCommandBeanList);
			logger.info("Inside getGatewayList Post Size ==== " + deviceCommandBeanList.getTotalElements());
			messageDto.setTotalKey(deviceCommandBeanList.getTotalElements());
			logger.info("Total Elements" + deviceCommandBeanList.getTotalElements());

			messageDto.setCurrentPage(deviceCommandBeanList.getNumber());
			logger.info("Current Page " + deviceCommandBeanList.getNumber());

			messageDto.setTotal_pages(deviceCommandBeanList.getTotalPages());
			logger.info("Total pages" + deviceCommandBeanList.getTotalPages());
			stopWatch.stop();
			logger.info("after getting response from getAllDeviceCommands method from DeviceCommand controller method :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity<>(messageDto, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting gateway(s)", exception);
			return new ResponseEntity<Object>(
					new ResponseBodyDTO<DeviceCommandListDTO>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(description = "API to get the Gateway ATC Commands curresponding to gatewayId and uuid")
	@PostMapping("/get-request")
	public ResponseEntity<MessageDTO<DeviceATCommandReqResponse>> getGatewayATCommandRequest(
			HttpServletRequest httpServletRequest,
			@RequestBody DeviceCommandRequestPayload deviceCommandRequestPayload) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info(
					"before getting response from getGatewayATCommandRequest method from DeviceCommand controller method :"
							+ stopWatch.prettyPrint());
			MessageDTO<DeviceATCommandReqResponse> messageDto = new MessageDTO<>(
					"Fetched GatewayCommands(s) Successfully", true);
			if (ValidationUtils.isEmpty(deviceCommandRequestPayload)
					|| ValidationUtils.isEmpty(deviceCommandRequestPayload.getUuid())) {
				return new ResponseEntity<MessageDTO<DeviceATCommandReqResponse>>(
						new MessageDTO<>("Please Send valide Data Uuid and  Device Id", null, false),
						HttpStatus.BAD_REQUEST);
			}
			messageDto.setBody(deviceCommandService.getATCommandResponse(deviceCommandRequestPayload.getUuid()));
			logger.info(
					"after getting response from getGatewayATCommandRequest method from DeviceCommand controller method :"
							+ stopWatch.prettyPrint());
			return new ResponseEntity<>(messageDto, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting gateway(s)", exception);
			return new ResponseEntity<MessageDTO<DeviceATCommandReqResponse>>(
					new MessageDTO<>(exception.getMessage(), null, false), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(description = "API to get the Gateway ATC Commands curresponding to gatewayId and uuid")
	@PostMapping("/get-latest-request")
	public ResponseEntity<MessageDTO<DeviceATCommandReqResponse>> getGatewayATCommandLatestRequest(
			HttpServletRequest httpServletRequest,
			@RequestBody DeviceCommandRequestPayload deviceCommandRequestPayload) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info(
					"before getting response from getGatewayATCommandRequest method from DeviceCommand controller method :"
							+ stopWatch.prettyPrint());
			MessageDTO<DeviceATCommandReqResponse> messageDto = new MessageDTO<>(
					"Fetched GatewayCommands(s) Successfully", true);
			if (ValidationUtils.isEmpty(deviceCommandRequestPayload)
					|| ValidationUtils.isEmpty(deviceCommandRequestPayload.getDeviceId())) {
				return new ResponseEntity<MessageDTO<DeviceATCommandReqResponse>>(
						new MessageDTO<>("Please Send valide Device Id", null, false), HttpStatus.BAD_REQUEST);
			}
			messageDto.setBody(
					deviceCommandService.getATCommandLatestResponse(deviceCommandRequestPayload.getDeviceId()));
			logger.info(
					"after getting response from getGatewayATCommandRequest method from DeviceCommand controller method :"
							+ stopWatch.prettyPrint());
			return new ResponseEntity<>(messageDto, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting gateway(s)", exception);
			return new ResponseEntity<MessageDTO<DeviceATCommandReqResponse>>(
					new MessageDTO<>(exception.getMessage(), null, false), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(description = "API to send the ATC Request to gateway for moble")
	@PostMapping("/send")
	public ResponseEntity<ResponseBodyDTO> sendAtRequestForMobile(@Validated @RequestBody Map<String, Object> sensorDetails,
			HttpServletRequest httpServletRequest) {
		try {
			ResponseBodyDTO response = deviceCommandService.sendAtcRequestForMobile(sensorDetails);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter sw = Logutils.exception(e);
			logger.info(sw.toString());
			return new ResponseEntity<>(new ResponseBodyDTO<>(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Operation(description = "API to refresh the ATC Request to gateway for mobile")
	@PostMapping("/refresh")
	public ResponseEntity<ResponseBodyDTO> refrshAtRequestForMobile(@Validated @RequestBody Map<String, Object> sensorDetails,
			HttpServletRequest httpServletRequest) {
		try {
			ResponseBodyDTO response = deviceCommandService.refreshAtcRequestForMobile(sensorDetails);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter sw = Logutils.exception(e);
			logger.info(sw.toString());
			return new ResponseEntity<>(new ResponseBodyDTO<>(false, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Operation(description = "API to check the device-command already exist")
	@PostMapping("/atc-request/device-command/exist")
	public ResponseEntity<ResponseBodyDTO> checkDeviceCommandExists(
			@Validated @RequestBody GatewayRequestPayload gatewayRequestPayload,
			HttpServletRequest httpServletRequest) throws Exception {
		UUID uuid = UUID.randomUUID();
		String messageUUID = uuid.toString();
		DeviceCommandRequest deviceCommandRequest = new DeviceCommandRequest();
		if (gatewayRequestPayload.getDeviceId() != null) {
			gatewayRequestPayload.setGatewayId(gatewayRequestPayload.getDeviceId());
		}
		deviceCommandRequest.setAtCommand(gatewayRequestPayload.getAtCommand());
		deviceCommandRequest.setDeviceId(gatewayRequestPayload.getGatewayId());
		DeviceCommandResponse deviceCommandResponse = deviceCommandService.checkDeviceCommandExists(deviceCommandRequest, messageUUID);
		return new ResponseEntity<>(new ResponseBodyDTO<>(true, "ATC device-command checked successfully", deviceCommandResponse),
				HttpStatus.OK);
	}

	@Operation(description = "API to get the device-commands for device")
	@GetMapping("/atc-request/{device_id}/device-commands")
	public ResponseEntity<ResponseBodyDTO> atcCache(@PathVariable("device_id") String deviceId) throws Exception {
		UUID uuid = UUID.randomUUID();
		String messageUUID = uuid.toString();
		DeviceCommandResponse deviceCommandResponse = deviceCommandService.atcCache(deviceId, messageUUID);
		if (deviceCommandResponse != null && deviceCommandResponse.getAtCommands() != null) {
			return new ResponseEntity<>(new ResponseBodyDTO<>(true,
					deviceCommandResponse.getAtCommands().size() + " ATC device-commands found in cache",
					deviceCommandResponse), HttpStatus.OK);
		}
		return new ResponseEntity<>(
				new ResponseBodyDTO<>(true, " device id " + deviceId + " not found in cache", deviceCommandResponse),
				HttpStatus.OK);
	}

	@Operation(description = "API to get the cargo command for device")
	@GetMapping("/device-cargo-command/{device_id}")
	public ResponseEntity<DeviceCommandStatusResponse> deviceCargoCommand(@PathVariable("device_id") String deviceId) throws Exception {
		UUID uuid = UUID.randomUUID();
		String messageUUID = uuid.toString();
		String status = deviceCommandService.deviceCargoCommand(deviceId, messageUUID);
		DeviceCommandStatusResponse deviceCommandStatusResponse = new DeviceCommandStatusResponse(status);
		return new ResponseEntity<>(deviceCommandStatusResponse, HttpStatus.OK);
	}

	@Operation(description = "API to get the camera command for device")
	@GetMapping("/device-camera-command/{device_id}")
	public ResponseEntity<DeviceCommandStatusResponse> deviceCameraCommand(@PathVariable("device_id") String deviceId) throws Exception {
		UUID uuid = UUID.randomUUID();
		String messageUUID = uuid.toString();
		String status = deviceCommandService.deviceCameraCommand(deviceId, messageUUID);
		DeviceCommandStatusResponse deviceCommandStatusResponse = new DeviceCommandStatusResponse(status);
		return new ResponseEntity<>(deviceCommandStatusResponse, HttpStatus.OK);
	}

	@Operation(description = "API to get the gladhand unlock command for device")
	@GetMapping("/gladhands-unlock/{device_id}")
	public ResponseEntity<DeviceCommandStatusResponse> deviceGladhandUnlockCommand(@PathVariable("device_id") String deviceId)
			throws Exception {
		UUID uuid = UUID.randomUUID();
		String messageUUID = uuid.toString();
		String status = deviceCommandService.deviceGladhandUnlockCommand(deviceId, messageUUID);
		DeviceCommandStatusResponse deviceCommandStatusResponse = new DeviceCommandStatusResponse(status);
		return new ResponseEntity<>(deviceCommandStatusResponse, HttpStatus.OK);
	}

	@Operation(description = "API to get the gladhand lock command for device")
	@GetMapping("/gladhands-lock/{device_id}")
	public ResponseEntity<DeviceCommandStatusResponse> deviceGladhandLockCommand(@PathVariable("device_id") String deviceId)
			throws Exception {
		UUID uuid = UUID.randomUUID();
		String messageUUID = uuid.toString();
		String status = deviceCommandService.deviceGladhandLockCommand(deviceId, messageUUID);
		DeviceCommandStatusResponse deviceCommandStatusResponse = new DeviceCommandStatusResponse(status);
		return new ResponseEntity<>(deviceCommandStatusResponse, HttpStatus.OK);
	}

	@Operation(description = "API to precheck UDP command for device")
	@GetMapping("/device-precheck-command/{device_id}")
	public ResponseEntity<DeviceCommandStatusResponse> devicePreCheckUDPCommand(@PathVariable("device_id") String deviceId) throws Exception {
		UUID uuid = UUID.randomUUID();
		String messageUUID = uuid.toString();
		String status = deviceCommandService.devicePreCheckUDPCommand(deviceId, messageUUID);
		DeviceCommandStatusResponse deviceCommandStatusResponse = new DeviceCommandStatusResponse(status);
		return new ResponseEntity<>(deviceCommandStatusResponse, HttpStatus.OK);
	}

	@Operation(description = "API to report UDP command for device")
	@GetMapping("/device-reportnow-command/{device_id}")
	public ResponseEntity<DeviceCommandStatusResponse> deviceReportNowUDPCommand(
			@PathVariable("device_id") String deviceId) throws Exception {
		UUID uuid = UUID.randomUUID();
		String messageUUID = uuid.toString();
		String status = deviceCommandService.deviceReportNowUDPCommand(deviceId, messageUUID);
		DeviceCommandStatusResponse deviceCommandStatusResponse = new DeviceCommandStatusResponse(status);
		return new ResponseEntity<>(deviceCommandStatusResponse, HttpStatus.OK);
	}

}