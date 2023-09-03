package com.pct.device.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import com.pct.common.controller.IApplicationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.model.Device;
import com.pct.common.util.JwtUser;
import com.pct.common.util.JwtUtil;
import com.pct.device.dto.DeviceListDTO;
import com.pct.device.dto.MessageDTO;
import com.pct.device.payload.BeaconDetailPayLoad;
import com.pct.device.payload.BeaconPayload;
import com.pct.device.payload.BeaconRequestPayload;
import com.pct.device.payload.GatewayPayload;
import com.pct.device.service.impl.BeaconServiceImpl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/beacon")
@Api(value = "/beacon", tags = "Beacon Management")
public class BeaconController implements IApplicationController<Device> {
	Logger logger = LoggerFactory.getLogger(BeaconController.class);
	
	//@Autowired
	//private JwtUtil jwtUtil;
	
	@Autowired
	private BeaconServiceImpl beaconService;

	@ApiOperation(value = "Get beacon details", notes = "API to get a beacon details", response = GatewayPayload.class, tags = {
			"Beacon Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get it", response = DeviceListDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping()
	public ResponseEntity<ResponseBodyDTO<List<BeaconPayload>>> getBeacon(HttpServletRequest httpServletRequest,
			@RequestParam(value = "can", required = false) String accountNumber,
			@RequestParam(value = "device-uuid", required = false) String gatewayUuid) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Before getting response from getBeacon method from beacon controller method :"
					+ stopWatch.prettyPrint());
			List<BeaconPayload> beaconDetails = beaconService.getBeaconDetails(accountNumber, gatewayUuid);
			stopWatch.stop();
			logger.info("After getting response from getBeacon method from beacon controller method :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity<ResponseBodyDTO<List<BeaconPayload>>>(
					new ResponseBodyDTO<List<BeaconPayload>>(true, "Fetched beacon(s) successfully", beaconDetails),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting gateway(s)", exception);
			return new ResponseEntity<ResponseBodyDTO<List<BeaconPayload>>>(
					new ResponseBodyDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Add beacon detail", notes = "API to add beacon detail", response = String.class, tags = {
			"Beacon Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Added Successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping()
	public ResponseEntity<ResponseDTO> addBeaconDetail(@RequestBody BeaconRequestPayload beaconRequestRequest,
			HttpServletRequest httpServletRequest) {
		Boolean status = false;
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : "+jwtUser.getUsername());
			logger.info("Request received to add beacon detail {}", beaconRequestRequest.toString());
			status = beaconService.addBeaconDetail(beaconRequestRequest, jwtUser.getUsername());
		} catch (Exception e) {
			logger.error("Exception occurred in adding of beacon detail", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Beacon details saved successfully"),
				HttpStatus.CREATED);
	}

	@ApiOperation(value = "Update beacon detail", notes = "API to update  beacon detail", response = String.class, tags = {
			"Beacon Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Updated beacon Successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PutMapping()
	public ResponseEntity<ResponseDTO> updateBeaconDetail(@RequestBody BeaconDetailPayLoad beaconDetailPayload,
			HttpServletRequest httpServletRequest) {
		Boolean status = false;
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : "+jwtUser.getUsername());
			logger.info("Request received to update beacon detail {}", beaconDetailPayload.toString());
			status = beaconService.updateBeaconDetail(beaconDetailPayload, jwtUser.getUsername());
		} catch (Exception e) {
			logger.error("Exception occurred in updating of beacon detail", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Beacon details updated successfully"),
				HttpStatus.CREATED);
	}

	@ApiOperation(value = "Delete the beacon details", notes = "API to delete the beacon details", response = String.class, tags = {
			"Beacon Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Success", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@DeleteMapping()
	public ResponseEntity<ResponseDTO> deleteDeviceDetail(@RequestParam(value = "_can", required = false) String can,
			@RequestParam(value = "_uuid", required = false) String uuid) {
		Boolean status = false;
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Request received to delete beacon detail {}" + stopWatch.prettyPrint());
			status = beaconService.deleteBeaconDetail(can, uuid);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Beacon details deleted successfully"),
					HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@ApiOperation(value = "Get beacon details with pagination", notes = "API to get beacon details with pagination", response = Object.class, tags = {
			"Beacon Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get beacon with pagination", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/page")
	public ResponseEntity<Object> getBeaconListWithPagination(
			@RequestParam(value = "can", required = true) String accountNumber,
			@RequestParam(value = "device-uuid", required = false) String deviceUuid,
			@RequestParam(value = "device-status", required = false) DeviceStatus deviceStatus,
			@RequestParam(value = "mac", required = false) String mac,
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestBody Map<String, String> filterValues, HttpServletRequest httpServletRequest) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info(
					"Before getting response from getBeaconListWithPagination method from beacon controller method :"
							+ stopWatch.prettyPrint());
			MessageDTO<Page<BeaconPayload>> messageDto = new MessageDTO<>("Fetched beacon(s) successfully", true);
			Page<BeaconPayload> device = beaconService.getBeaconWithPagination(accountNumber, deviceUuid, deviceStatus,
					mac, filterValues, getPageable(page - 1, size, sort, order));
			messageDto.setBody(device);
			logger.info("Inside getBeaconList Post Size ==== " + device.getTotalElements());
			messageDto.setTotalKey(device.getTotalElements());
			logger.info("Total Elements" + device.getTotalElements());
			messageDto.setCurrentPage(device.getNumber());
			logger.info("Current Page " + device.getNumber());
			messageDto.setTotal_pages(device.getTotalPages());
			logger.info("Total pages" + device.getTotalPages());
			stopWatch.stop();
			logger.info("After getting response from getBeaconListWithPagination method from beacon controller method :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity(messageDto, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting beacon(s)", exception);
			return new ResponseEntity(new ResponseBodyDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
