package com.pct.device.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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
import com.pct.common.controller.IApplicationController;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.model.Device;
import com.pct.common.util.JwtUser;
import com.pct.common.util.JwtUtil;
import com.pct.common.constant.DeviceStatus;
import com.pct.device.dto.DeviceListDTO;
import com.pct.device.dto.MessageDTO;
import com.pct.device.dto.SensorDataDTO;
import com.pct.device.payload.GatewayPayload;
import com.pct.device.payload.RefreshUpdatedMacAddressReqPayload;
import com.pct.device.payload.RetriveDeviceSensorRequest;
import com.pct.device.payload.SensorDetailPayLoad;
import com.pct.device.payload.SensorPayLoad;
import com.pct.device.payload.SensorRequestPayload;
import com.pct.device.payload.UpdateMacAddressRequest;
import com.pct.device.service.impl.SensorServiceImpl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

@RestController
@RequestMapping("/sensor")
@Api(value = "/sensor", tags = "Sensor Management")
public class SensorController implements IApplicationController<Device> {

	@Autowired
	private SensorServiceImpl sensorService;

	Logger logger = LoggerFactory.getLogger(SensorController.class);

	@ApiOperation(value = "Get sensor detail(s)", notes = "API to get a sensor detail(s)", response = SensorPayLoad.class, tags = {
			"Sensor Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get it", response = SensorPayLoad.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping()
	public ResponseEntity<ResponseBodyDTO<List<SensorPayLoad>>> getSensor(HttpServletRequest httpServletRequest,
			@RequestParam(value = "can", required = false) String accountNumber,
			@RequestParam(value = "device-uuid", required = false) String gatewayUuid) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Before getting response from getSensor method from sensor controller method :"
					+ stopWatch.prettyPrint());
			List<SensorPayLoad> sensorDetails = sensorService.getSensorDetails(accountNumber, gatewayUuid);
			stopWatch.stop();
			logger.info("After getting response from getSensor method from sensor controller method :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity<ResponseBodyDTO<List<SensorPayLoad>>>(
					new ResponseBodyDTO<List<SensorPayLoad>>(true, "Fetched sensor(s) successfully", sensorDetails),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting gateway(s)", exception);
			return new ResponseEntity(new ResponseBodyDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get sensor detail(s) with pagination", notes = "API for get a sensor detail(s) with pagination", response = Object.class, tags = {
			"Sensor Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get sensor with pagination", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/page")
	public ResponseEntity<Object> getSensorListWithPagination(
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
			logger.info("Before getting response from getSensorListWithPagination method from sensor controller :"
					+ stopWatch.prettyPrint());
			MessageDTO<Page<SensorPayLoad>> messageDto = new MessageDTO<>("Fetched sensor(s) successfully", true);
			Page<SensorPayLoad> device = sensorService.getSensorWithPagination(accountNumber, deviceUuid, deviceStatus,
					mac, filterValues, getPageable(page - 1, size, sort, order));
			messageDto.setBody(device);
			logger.info("Inside getSensorList Post Size ==== " + device.getTotalElements());
			// messageDto.setTotalKey(device.getTotalElements());
			// logger.info("Total Elements" + device.getTotalElements());

			// messageDto.setCurrentPage(device.getNumber());
			logger.info("Current Page " + device.getNumber());

			// messageDto.setTotal_pages(device.getTotalPages());
			logger.info("Total pages" + device.getTotalPages());
			stopWatch.stop();
			logger.info("After getting response from getSensorListWithPagination method from sensor controller :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity(messageDto, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting sensor(s)", exception);
			return new ResponseEntity<Object>(new ResponseBodyDTO<DeviceListDTO>(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Add sensor detail", notes = "API to add sensor detail", response = String.class, tags = {
			"Sensor Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Added Successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping()
	public ResponseEntity<ResponseDTO> addSensorDetail(@RequestBody SensorRequestPayload sensorRequestRequest,
			HttpServletRequest httpServletRequest) {
		Boolean status = false;
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			logger.info("Request received to add sensor detail {}", sensorRequestRequest.toString());
			status = sensorService.addSensorDetail(sensorRequestRequest, jwtUser.getUsername());
		} catch (Exception e) {
			logger.error("Exception occurred in adding of sensor detail", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Sensor details saved successfully"),
				HttpStatus.CREATED);
	}

	@ApiOperation(value = "Update sensor detail", notes = "API to update sensor detail", response = String.class, tags = {
			"Sensor Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Updated sensor Successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PutMapping()
	public ResponseEntity<ResponseDTO> updateSensorDetail(@RequestBody SensorDetailPayLoad sensorDetailPayload,
			HttpServletRequest httpServletRequest) {
		Boolean status = false;
		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			logger.info("Request received to update sensor detail {}", sensorDetailPayload.toString());
			status = sensorService.updateSensorDetail(sensorDetailPayload, jwtUser.getUsername());
		} catch (Exception e) {
			logger.error("Exception occurred in updating of sensor detail", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Sensor details updated successfully"),
				HttpStatus.CREATED);
	}

	@ApiOperation(value = "Delete the sensor details", notes = "API to delete the sensor details", response = String.class, tags = {
			"Sensor Management" })
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
			logger.info("Request received to delete sensor detail {}" + stopWatch.prettyPrint());
			status = sensorService.deleteSensorDetail(can, uuid);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Sensor details deleted successfully"),
					HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping("/sensor-list")
	public ResponseEntity<ResponseBodyDTO<List<SensorDataDTO>>> getSensorList(HttpServletRequest httpServletRequest) {
		try {
			List<SensorDataDTO> sensorList = sensorService.getSensorList();

			return new ResponseEntity<ResponseBodyDTO<List<SensorDataDTO>>>(
					new ResponseBodyDTO<List<SensorDataDTO>>(true, "Fetched Sensor List(s) Successfully", sensorList),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting sensor(s)", exception);
			return new ResponseEntity<ResponseBodyDTO<List<SensorDataDTO>>>(
					new ResponseBodyDTO<List<SensorDataDTO>>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	@ApiOperation(value = "Update sensor details mac address", notes = "API to update sensor details mac address", response = String.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Updated Successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PutMapping("/update-sensor-details")
	public ResponseEntity<ResponseDTO> updateSensorDetailsMacAddress(
			@Valid @RequestBody Map<String, Object> sensorDetails) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Before getting response from updateSensorDetailsMacAddress method from sensor controller :"
					+ stopWatch.prettyPrint());
			MessageDTO message = sensorService.updateSensorDetailsMacAddress(sensorDetails);
			logger.info("After getting response from updateSensorDetailsMacAddress method from sensor controller :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(message.getStatus(), message.getMessage()),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception while updating Sensor Details Mac Address", exception);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	@ApiOperation(value = "Refresh updated sensor details mac address", notes = "API to Refresh Update sensor details mac address", response = String.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Updated Successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/refresh-sensor-details")
	public ResponseEntity<ResponseDTO> refreshUpdatedSensorDetailsMacAddress(
			@Valid @RequestBody RefreshUpdatedMacAddressReqPayload sensorDetails) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Before getting response from refreshUpdatedSensor method from sensor controller :"
					+ stopWatch.prettyPrint());
			MessageDTO message = sensorService.refreshUpdatedSensor(sensorDetails);
			logger.info("After getting response from refreshUpdatedSensor method from sensor controller :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(message.getStatus(), message.getMessage()),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception while refreshin updated Sensor Details Mac Address", exception);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Update sensor details mac address", notes = "API to update sensor details mac address", response = String.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Updated Successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/get-sensor-details")
	public ResponseEntity<ResponseDTO> getSensorDetails(
			@Valid @RequestBody RetriveDeviceSensorRequest retriveDeviceSensorRequest) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Before getting response from updateSensorDetailsMacAddress method from sensor controller :"
					+ stopWatch.prettyPrint());
			MessageDTO message = sensorService.getAndAddSensorDetails(retriveDeviceSensorRequest);
			logger.info("After getting response from updateSensorDetailsMacAddress method from sensor controller :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(message.getStatus(), message.getMessage()),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception while updating Sensor Details Mac Address", exception);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Update sensor details mac address", notes = "API to update sensor details mac address", response = String.class, tags = {
			"Device Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Updated Successfully", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping("/get-latest-sensor-details")
	public ResponseEntity<ResponseDTO> getLatestSensorDetails(
			@Valid @RequestBody RetriveDeviceSensorRequest retriveDeviceSensorRequest) {
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			logger.info("Before getting response from updateSensorDetailsMacAddress method from sensor controller :"
					+ stopWatch.prettyPrint());
			MessageDTO message = sensorService.getAndAddLatestSensorDetails(retriveDeviceSensorRequest);
			logger.info("After getting response from updateSensorDetailsMacAddress method from sensor controller :"
					+ stopWatch.prettyPrint());
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(message.getStatus(), message.getMessage()),
					HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception while updating Sensor Details Mac Address", exception);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	@PutMapping("/mac-address")
    public ResponseEntity<ResponseDTO> updateMacAddressForSensor(HttpServletRequest httpServletRequest,
                                                                  @RequestBody UpdateMacAddressRequest updateMacAddressRequest) {
        logger.info("Request received for updating sensor uuid {} with mac address {}", updateMacAddressRequest.getUuid(), updateMacAddressRequest.getMacAddress());
        try {
        	JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();     
            Boolean status = sensorService.updateSensorMacAddress(updateMacAddressRequest,jwtUser.getUsername());
            return new ResponseEntity<>(new ResponseDTO(status, "Successfully updated MAC Address for Sensor"),
                    HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Exception occurred while updating sensor mac address", exception);
            return new ResponseEntity<ResponseDTO>(
                    new ResponseDTO(false, exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
