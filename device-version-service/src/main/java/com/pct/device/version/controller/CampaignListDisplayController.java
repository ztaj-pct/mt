package com.pct.device.version.controller;

import com.pct.common.controller.IApplicationController;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.util.JwtUser;
import com.pct.common.util.JwtUtil;
import com.pct.device.version.dto.MessageDTO;
import com.pct.device.version.exception.BadRequestException;
import com.pct.device.version.exception.BaseMessageException;
import com.pct.device.version.exception.DeviceVersionException;
import com.pct.device.version.model.CampaignGatewayDetails;
import com.pct.device.version.model.CampaignListDisplay;
import com.pct.device.version.model.CampaignStatsPayloadList;
import com.pct.device.version.model.CampaignStep;
import com.pct.device.version.payload.*;
import com.pct.device.version.repository.projections.CampaignIdAndNameView;
import com.pct.device.version.service.ICampaignService;
import com.pct.device.version.util.CampaignExcelExporter;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/campaign/v1")
public class CampaignListDisplayController implements IApplicationController<CampaignListDisplay> {

	Logger logger = LoggerFactory.getLogger(CampaignListDisplayController.class);

	private static Integer DEFAULT_PAGE_SIZE = 10000;
	@Autowired
	private ICampaignService campaignService;
	@Autowired
	private JwtUtil jwtUtil;

	@PostMapping("/fetchListCampaign")
	public  Page<CampaignListDisplay>  fetchListCampaign(@RequestParam(value = "_page", required = false) Integer page,
															@RequestParam(value = "_limit", required = false) Integer pageSize,
															@RequestParam(value = "_sort", required = false) String sort,
															@RequestParam(value = "_order", required = false) String order,
															@RequestBody Map<String, String> filterValues,
															HttpServletRequest httpServletRequest) throws IOException {
		//Long userId = jwtUtil.getUserIdFromRequest(httpServletRequest);
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.info("Inside CampaignListDisplay Controller Level");
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
				sort = "campaignName";
			}
			return	campaignService.findALLCampaignList(filterValues,
					getPageable(page - 1, pageSize, sort, order),jwtUser.getUsername());

		} catch (BaseMessageException e) {
			logger.error("Exception while fetching Campaign ", e);

		} catch (Exception e) {
			logger.error("Exception while fetching Campaign ", e);
		}
		return null;
	}

	@GetMapping("/fetchAllCampaignData")
	public List<CampaignListDisplay> fetchAllCampaignData(HttpServletRequest httpServletRequest) throws IOException {
		Long userId = jwtUtil.getUserIdFromRequest(httpServletRequest);
		logger.info("Inside fetchAllCampaignData Controller Level");
		try {

			return campaignService.findALLCampaignDataList(userId);

		} catch (BaseMessageException e) {
			logger.error("Exception while fetching Campaign data", e);

		} catch (Exception e) {
			logger.error("Exception while fetching Campaign data", e);
		}
		return null;
	}

	@GetMapping("/fetchAllCampaignData/{status}")
	public List<CampaignListDisplay> fetchAllCampaignDataForStatus(HttpServletRequest httpServletRequest,
			@Validated @PathVariable("status") String status) throws IOException {
		Long userId = jwtUtil.getUserIdFromRequest(httpServletRequest);
		logger.info("Inside fetchAllCampaignData Controller Level");
		try {

			List<CampaignListDisplay> campaignListDisplay = campaignService.findALLCampaignDataList(userId).stream()
					.filter(m -> m.getCampaignStatus().equalsIgnoreCase(status)).collect(Collectors.toList());

			return campaignListDisplay;
			// return campaignService.findALLCampaignDataList(userId);

		} catch (BaseMessageException e) {
			logger.error("Exception while fetching Campaign data", e);

		} catch (Exception e) {
			logger.error("Exception while fetching Campaign data", e);
		}
		return null;
	}

}
