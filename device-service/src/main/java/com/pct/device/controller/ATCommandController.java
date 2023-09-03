package com.pct.device.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.controller.IApplicationController;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.model.Device;
import com.pct.common.payload.DeviceATCommandReqResponse;
import com.pct.common.util.JwtUser;
import com.pct.common.util.JwtUtil;
import com.pct.device.Bean.DeviceCommandBean;
import com.pct.device.dto.DeviceReportDTO;
import com.pct.device.dto.GatewayCommandResponse;
import com.pct.device.dto.MessageDTO;
import com.pct.device.payload.ATCommandRequestPayload;
import com.pct.device.payload.GatewayUploadRequest;
import com.pct.device.service.impl.ATCommandServiceImpl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/atcommand")
@Api(value = "/atcommand", tags = "ATCommand Management")
public class ATCommandController implements IApplicationController<GatewayCommandResponse> {

	private static final Logger logger = LoggerFactory.getLogger(ATCommandController.class);

	//@Autowired
	//private JwtUtil jwtUtil;

	@Autowired
	private ATCommandServiceImpl atcommandService;

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

			List<GatewayCommandResponse> atcReqQueue = atcommandService.getQueuedATCReq(deviceId);
			Pageable pageable = getPageable(page - 1, size, sort, order);
			int start = (int) pageable.getOffset();
			int end = Math.min((start + pageable.getPageSize()), atcReqQueue.size());
			Page<GatewayCommandResponse> page1 = new PageImpl<>(atcReqQueue.subList(start, end), pageable,
					atcReqQueue.size());
			MessageDTO<Page<GatewayCommandResponse>> messageDto = new MessageDTO<>(
					"Fetched Queued At Request(s) Successfully", true);
			messageDto.setBody(page1);
			messageDto.setTotalKey(page1.getTotalElements());
			return new ResponseEntity(messageDto, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Queued ATC(s)", exception);
			return new ResponseEntity(new ResponseBodyDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Send ATC Request", notes = "API to send ATC request", response = String.class, tags = {
			"ATCommand Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Added Successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/send-atc-request")
	public ResponseEntity<ResponseBodyDTO> sendATCRequest(@RequestBody ATCommandRequestPayload atcRequestPayload,
			HttpServletRequest httpServletRequest) {
		Boolean status = false;
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : "+jwtUser.getUsername());
			logger.info("Request received to sendATCRequest {}", atcRequestPayload.toString());
			DeviceATCommandReqResponse atcResponse = atcommandService.getATCResponse(atcRequestPayload, jwtUser.getUsername());
			return new ResponseEntity<ResponseBodyDTO>(
					new ResponseBodyDTO(true, "ATC Request sent successfully", atcResponse), HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while sending ATC Request(s)", exception);
			return new ResponseEntity(new ResponseBodyDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "delete ATC Request", notes = "API to delete ATC request", response = String.class, tags = {
			"ATCommand Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Added Successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/delete-atc-request")
	public ResponseEntity<ResponseBodyDTO> deleteATCRequest(@RequestBody ATCommandRequestPayload atcRequestPayload,
			HttpServletRequest httpServletRequest) {
		Boolean status = false;
		try {
			//Long userId = jwtUtil.getUserIdFromRequest(httpServletRequest);
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Request received to sendATCRequest {}", atcRequestPayload.toString());
			String atcResponse = atcommandService.deletetATCRequest(atcRequestPayload, jwtUser.getUsername());
			return new ResponseEntity<ResponseBodyDTO>(
					new ResponseBodyDTO(true, "Delete AT Command successfully", atcResponse), HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while deleting ATC Request(s)", exception);
			return new ResponseEntity(new ResponseBodyDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/gateway-atc-request")
	public ResponseEntity<Object> getGatewayAtRequest(HttpServletRequest httpServletRequest,
			@RequestParam(value = "gateway_Id", required = false) String deviceId,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "limit", required = false) Integer size,
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
			Page<DeviceCommandBean> deviceCommandBeanList = atcommandService.getDeviceCommandWithPagination(deviceId,
					getPageable(page - 1, size, sort, order));
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
			return new ResponseEntity(messageDto, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting gateway(s)", exception);
			return new ResponseEntity<Object>(new ResponseBodyDTO<Object>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
