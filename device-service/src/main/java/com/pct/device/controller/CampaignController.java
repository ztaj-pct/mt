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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import com.pct.device.Bean.DeviceCommandBean;
import com.pct.device.dto.CampaignHistoryDeviceResponse;
import com.pct.device.dto.CampaignHistoryPayloadResponse;
import com.pct.device.dto.CampaignInstallDeviceResponse;
import com.pct.device.dto.CurrentCampaignResponse;
import com.pct.device.dto.DeviceReportDTO;
import com.pct.device.dto.GatewayCommandResponse;
import com.pct.device.dto.MessageDTO;
import com.pct.device.payload.ATCommandRequestPayload;
import com.pct.device.payload.GatewayUploadRequest;
import com.pct.device.service.impl.ATCommandServiceImpl;
import com.pct.device.service.impl.CampaignServiceImpl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/campaign")
@Api(value = "/campaign", tags = "Campaign Management")
public class CampaignController implements IApplicationController<GatewayCommandResponse> {

	private static final Logger logger = LoggerFactory.getLogger(CampaignController.class);

	@Autowired
	private CampaignServiceImpl campaignService;

	@ApiOperation(value = "Get Device Campaign History By IMEI", notes = "API to get evice Campaign History By IMEI", response = ResponseBodyDTO.class, tags = {
			"Campaign Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get it", response = ResponseBodyDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/getDeviceCampaignHistoryByImei")
	public ResponseEntity<ResponseBodyDTO> getDeviceCampaignHistoryByImei(HttpServletRequest httpServletRequest,
			@RequestParam("deviceId") String imei, @RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order) {
		try {
			logger.info(
					"Before getting response from getDeviceCampaignHistoryByImei method from atcommand controller method :");
			if (page == null) {
				page = 1;
			} else if (page == -1) {
				throw new Exception("invalid.page_index.in.input");
			}

			if (size == null) {
				size = 25;
			}
			List<CampaignHistoryDeviceResponse> campaignHistoryResponse = campaignService
					.getDeviceCampaignHistoryByImei(imei);
			Pageable pageable = getPageable(page - 1, size, sort, order);
			int start = (int) pageable.getOffset();
			int end = Math.min((start + pageable.getPageSize()), campaignHistoryResponse.size());
			Page<CampaignHistoryDeviceResponse> page1 = new PageImpl<>(campaignHistoryResponse.subList(start, end),
					pageable, campaignHistoryResponse.size());
			MessageDTO<Page<CampaignHistoryDeviceResponse>> messageDto = new MessageDTO<>(
					"Fetched Device Campaign History Successfully", true);
			messageDto.setBody(page1);
			messageDto.setTotalKey(page1.getTotalElements());
			return new ResponseEntity(campaignHistoryResponse, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting getDeviceCampaignHistoryByImei", exception);
			return new ResponseEntity(new ResponseBodyDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get Campaign Installed DeviceHistory By IMEI", notes = "API to get Campaign Installed Device", response = ResponseBodyDTO.class, tags = {
			"Campaign Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get it", response = ResponseBodyDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/getCampaignInstalledDevice")
	public ResponseEntity<ResponseBodyDTO> getCampaignInstalledDevice(HttpServletRequest httpServletRequest,
			@RequestParam("deviceId") String imei, @RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order) {
		try {
			logger.info(
					"Before getting response from getCampaignInstalledDevice method from atcommand controller method :");
			if (page == null) {
				page = 1;
			} else if (page == -1) {
				throw new Exception("invalid.page_index.in.input");
			}

			if (size == null) {
				size = 25;
			}
			List<CampaignInstallDeviceResponse> campaignDeviceResponse = campaignService
					.getCampaignInstalledDevice(imei);
			Pageable pageable = getPageable(page - 1, size, sort, order);
			int start = (int) pageable.getOffset();
			int end = Math.min((start + pageable.getPageSize()), campaignDeviceResponse.size());
			Page<CampaignInstallDeviceResponse> page1 = new PageImpl<>(campaignDeviceResponse.subList(start, end),
					pageable, campaignDeviceResponse.size());
			MessageDTO<Page<CampaignInstallDeviceResponse>> messageDto = new MessageDTO<>(
					"Fetched Device Campaign History Successfully", true);
			messageDto.setBody(page1);
			messageDto.setTotalKey(page1.getTotalElements());
			return new ResponseEntity(campaignDeviceResponse, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Campaign Install Device Response", exception);
			return new ResponseEntity(new ResponseBodyDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get Campaign Installed DeviceHistory By IMEI", notes = "API to get Campaign Installed Device", response = ResponseBodyDTO.class, tags = {
			"Campaign Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get it", response = ResponseBodyDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/current-campaign")
	public ResponseEntity<ResponseBodyDTO> getCurrentCampaign(HttpServletRequest httpServletRequest,
			@RequestParam("deviceId") String imei) {
		try {
			logger.info(
					"Before getting response from getCampaignInstalledDevice method from atcommand controller method :");
			CurrentCampaignResponse campaignDeviceResponse = campaignService
					.getCurrentCampaign(imei);
			MessageDTO<Page<CampaignInstallDeviceResponse>> messageDto = new MessageDTO<>(
					"Fetched Device Campaign History Successfully", true);
			return new ResponseEntity(campaignDeviceResponse, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while getting Campaign Install Device Response", exception);
			return new ResponseEntity(new ResponseBodyDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Get Campaign Installed DeviceHistory By IMEI", notes = "API to get Campaign Installed Device", response = ResponseBodyDTO.class, tags = {
			"Campaign Management" })
	@ApiResponses(value = { @ApiResponse(code = 201, message = "get it", response = ResponseBodyDTO.class),
			@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	@GetMapping("/campaign-history")
	public ResponseEntity<ResponseBodyDTO> campaignHistory(HttpServletRequest httpServletRequest,
			@RequestParam("deviceId") String imei, @RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order) {
		try {
			logger.info(
					"Before getting response from getCampaignInstalledDevice method from atcommand controller method :");
			if (page == null) {
				page = 1;
			} else if (page == -1) {
				throw new Exception("invalid.page_index.in.input");
			}

			if (size == null) {
				size = 25;
			}
			List<CampaignHistoryPayloadResponse> campaignHistoryPayload = campaignService.campaignHistory(imei);
			Pageable pageable = getPageable(page - 1, size, sort, order);
			int start = (int) pageable.getOffset();
			int end = Math.min((start + pageable.getPageSize()), campaignHistoryPayload.size());
			Page<CampaignHistoryPayloadResponse> page1 = new PageImpl<>(campaignHistoryPayload.subList(start, end),
					pageable, campaignHistoryPayload.size());
			MessageDTO<Page<CampaignHistoryPayloadResponse>> messageDto = new MessageDTO<>(
					"Fetched Device Campaign History Successfully", true);
			messageDto.setBody(page1);
			messageDto.setTotalKey(page1.getTotalElements());
			return new ResponseEntity(campaignHistoryPayload, HttpStatus.OK);
		} catch (Exception exception) {
			logger.error("Exception occurred while Device Campaign History", exception);
			return new ResponseEntity(new ResponseBodyDTO(false, exception.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
