package com.pct.device.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.controller.IApplicationController;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.model.Device;
import com.pct.common.model.DeviceQa;
import com.pct.common.util.JwtUser;
import com.pct.device.payload.DeviceQaRequest;
import com.pct.device.payload.DeviceQaResponse;
import com.pct.device.service.IDeviceQAService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/deviceQA")
@Api(value = "/deviceQA", tags = "DeviceQA Management")
public class DeviceQAController implements IApplicationController<DeviceQa> {
	
	@Autowired
	private IDeviceQAService deviceQAService;
	
	
	
	Logger logger = LoggerFactory.getLogger(DeviceQAController.class);

	@PostMapping("/add-qa")
	public ResponseEntity<ResponseDTO> addDeviceDetailQa(@RequestBody DeviceQaRequest deviceQaRequest,
			HttpServletRequest httpServletRequest) {
		Boolean status = false;
		try {
			logger.info("Request received to add device detail Qa {}", deviceQaRequest.toString());
			status = deviceQAService.addQaDeviceDetail(deviceQaRequest);
		} catch (Exception e) {
			logger.error("Exception occurred in adding of deviceQa detail", e);
			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Device Qa details saved successfully"),
				HttpStatus.CREATED);
	}
	
//	@PostMapping("/add-device-qa")
//	public ResponseEntity<ResponseDTO> addDeviceQAandDevice1(HttpServletRequest httpServletRequest) {
//		Boolean status = false;
//		try {
//			logger.info("Request received to add QA and Device detail", httpServletRequest.toString());
//			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//			logger.info("Username : " + jwtUser.getUsername());
//			status = deviceQAService.addQADeviceDetail1(httpServletRequest,jwtUser.getUsername());
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error("Exception occurred in adding of DeviceQa detail", e);
//			return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, e.getMessage()),
//					HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//		return new ResponseEntity<ResponseDTO>(new ResponseDTO(status, "Device Qa details saved successfully"),
//				HttpStatus.CREATED);
//	}
	
	@PostMapping("/add-device-qa")
	public ResponseEntity<String> addDeviceQAandDevice(HttpServletRequest httpServletRequest) {
		String body = "";
		try {
			logger.info("Request received to add QA and Device detail", httpServletRequest.toString());
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			body = deviceQAService.addQADeviceDetail(httpServletRequest, jwtUser.getUsername());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception occurred in adding of DeviceQa detail", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(body, HttpStatus.CREATED);
	}
	
	@GetMapping("/device_qa")
	public ResponseEntity<ResponseBodyDTO<List<DeviceQaResponse>>> getAllDeviceQaDetail(
			HttpServletRequest httpServletRequest) {
		logger.info("inside deviceQA");
		try {
			List<DeviceQaResponse> ProductMasterList = deviceQAService.getAllDeviceQa();
			return new ResponseEntity<ResponseBodyDTO<List<DeviceQaResponse>>>(
					new ResponseBodyDTO<List<DeviceQaResponse>>(true, "Fetched deviceQa List(s) successfully",
							ProductMasterList),
					HttpStatus.OK);
		} catch (Exception exception) {
			exception.printStackTrace();
			logger.error("Exception occurred while getting deviceQa", exception);
			return new ResponseEntity<ResponseBodyDTO<List<DeviceQaResponse>>>(
					new ResponseBodyDTO<List<DeviceQaResponse>>(false, exception.getMessage(), null),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PutMapping("/device_qa")
	public ResponseEntity<ResponseDTO> upDateDeviceQaDetail(@Valid @RequestBody DeviceQaRequest deviceQaRequest) {
		try {
			System.out.println("deviceQaRequest:" + deviceQaRequest);
			if (deviceQaRequest != null) {
				deviceQAService.updateDeviceQa(deviceQaRequest);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Failed while updating deviceQa ", e);
			throw new RuntimeException("Failed while updating deviceQa ");
		}
		return new ResponseEntity<ResponseDTO>(new ResponseDTO(true, "DeviceQa updated successfully"),
				HttpStatus.CREATED);
	}
	
}