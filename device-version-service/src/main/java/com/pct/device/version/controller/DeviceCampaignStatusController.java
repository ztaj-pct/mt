package com.pct.device.version.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.controller.IApplicationController;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.util.JwtUser;
import com.pct.device.version.dto.MessageDTO;
import com.pct.device.version.exception.BadRequestException;
import com.pct.device.version.exception.BaseMessageException;
import com.pct.device.version.payload.CampaignDeviceDetail;
import com.pct.device.version.service.IDeviceCampaignStatusService;

@RestController
@RequestMapping("/campaign/v2/device-campaign-status")
public class DeviceCampaignStatusController implements IApplicationController<CampaignDeviceDetail> {

	Logger logger = LoggerFactory.getLogger(DeviceCampaignStatusController.class);

	private static Integer DEFAULT_PAGE_SIZE = 10000000;

	@Autowired
	private IDeviceCampaignStatusService deviceCampaignStatusService;

	@PostMapping("/fetchAll")
	public ResponseEntity<Object> getCampaignDeviceDetails(@RequestParam(value = "uuid") String uuid,
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_limit", required = false) Integer pageSize,
			@RequestParam(value = "_sort", defaultValue = "deviceId") String sort,
			@RequestParam(value = "_order", defaultValue = "asc") String order,
			@RequestBody Map<String, String> filterValues, HttpServletRequest httpServletRequest) {
		logger.info("Inside getCampaignDeviceDetails Controller Level");
		try {
			if (page == null) {
				page = 1;
			} else if (page == -1 || page == 0) {
				throw new BadRequestException("invalid.page_index.in.input");
			}
			if (pageSize == null)
				pageSize = 10;
			if (pageSize > DEFAULT_PAGE_SIZE)
				pageSize = DEFAULT_PAGE_SIZE;
			if (sort.equalsIgnoreCase("uuid")) {
				sort = "deviceId";
			}
			if (sort.equalsIgnoreCase("lastReport")) {
				sort = "lastReportedAt";
			}
			MessageDTO<Object> messageDto = new MessageDTO<>("Device Campaign Status Fetched Successfully", true);
			Page<CampaignDeviceDetail> campaignDeviceDetailList = deviceCampaignStatusService
					.getCampaignDeviceDetails(uuid, filterValues, getPageable(page - 1, pageSize, sort, order));

			messageDto.setBody(campaignDeviceDetailList.getContent());
			messageDto.setTotalKey(campaignDeviceDetailList.getTotalElements());
			messageDto.setCurrentPage(campaignDeviceDetailList.getNumber() + 1);
			messageDto.setTotal_pages(campaignDeviceDetailList.getTotalPages());

			return new ResponseEntity<>(messageDto, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Exception while fetching Device Campaign Status ", e);
			return new ResponseEntity<>(new ResponseBodyDTO<>(false, "Internal server error"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
