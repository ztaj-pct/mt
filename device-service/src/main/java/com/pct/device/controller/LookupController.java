package com.pct.device.controller;

import com.pct.common.controller.IApplicationController;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.util.JwtUtil;
import com.pct.device.dto.MessageDTO;
import com.pct.device.model.Lookup;
import com.pct.device.payload.LookupPayload;
import com.pct.device.service.ILookupService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/config-lookup")
@Api(value = "/config-lookup", tags = "Config-lookup Management")
public class LookupController implements IApplicationController<Lookup> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LookupController.class);

	//@Autowired
	//JwtUtil JwtUtil;

	@Autowired
	private ILookupService lookupService;

	@ApiOperation(value = "Get all the active installer organisations", notes = "Api to get the all active installer organisations", response = String.class, tags = {
			"Config-lookup Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Fetched Asset Configurations", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping()
	public ResponseEntity<MessageDTO<Map<String, List<LookupPayload>>>> getAllActiveInstallerOrganisations(
			HttpServletRequest request, @RequestParam(value = "type", required = true) List<String> type) {
		logger.info("Inside getAllActiveOrganisations Method");
		try {
			Map<String, List<LookupPayload>> assetConfiguration = lookupService.getAllAssetConfiguration(type);
			return new ResponseEntity<MessageDTO<Map<String, List<LookupPayload>>>>(
					new MessageDTO<Map<String, List<LookupPayload>>>("Fetched Asset Configurations",
							assetConfiguration),
					HttpStatus.OK);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@ApiOperation(value = "Create a new package", notes = "Api to create a new package", response = ResponseBodyDTO.class, tags = {
			"Config-lookup Management" })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully saved Lookup", response = ResponseBodyDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@PostMapping()
	public ResponseEntity<ResponseBodyDTO> createPackage(@RequestBody LookupPayload saveLookupPayload) {
		try {
			logger.info("Request received for saveLookupPayload {}", saveLookupPayload.getField());
			lookupService.saveLookup(saveLookupPayload);
			String message = "Successfully saved Lookup ";
			return new ResponseEntity(new ResponseBodyDTO(true, message), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception while saving Lookup ", e);
			return new ResponseEntity(new ResponseBodyDTO(false, "Exception occurred while saving Lookup"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
