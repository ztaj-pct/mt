package com.pct.device.controller;

import java.util.HashMap;
import java.util.List;
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
import com.pct.common.model.MaintenanceReportHistory;
import com.pct.common.util.JwtUser;
import com.pct.device.dto.MaintenanceReportDTO;
import com.pct.device.dto.MessageDTO;
import com.pct.device.exception.DeviceException;
import com.pct.device.payload.MaintenanceReportHistoryPayload;
import com.pct.device.service.IMaintenanceReportService;
import com.pct.device.util.AppUtility;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/maintenance-report")
@Api(value = "/maintenance-report")
public class MaintenanceReportController implements IApplicationController<MaintenanceReportHistory> {

	Logger logger = LoggerFactory.getLogger(MaintenanceReportController.class);

	@Autowired
	private IMaintenanceReportService maintenanceReportService;

	@PostMapping
	public ResponseEntity<ResponseBodyDTO<Map<String, Object>>> addMaintenanceHistory(
			@RequestBody MaintenanceReportDTO maintenanceReportDTO, HttpServletRequest httpServletRequest) {
		MaintenanceReportHistory maintenanceReportHistory = new MaintenanceReportHistory();
		Map<String, Object> obj = new HashMap<>();

		

		try {
			JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			logger.info("Username : " + jwtUser.getUsername());
			logger.info("Request received to create Maintenance Report History {}", maintenanceReportDTO.toString());
			maintenanceReportHistory = maintenanceReportService.createMaintenaceRportHistory(maintenanceReportDTO,
					jwtUser.getUsername());
			obj.put("maintenance_uuid", maintenanceReportHistory.getUuid());
			obj.put("service_time", maintenanceReportHistory.getServiceDateTime());
		} catch (Exception e) {
			logger.error("Exception occurred in creating Maintenance Report History", e);
			return new ResponseEntity<ResponseBodyDTO<Map<String, Object>>>(
					new ResponseBodyDTO<Map<String, Object>>(false, e.getMessage(), obj),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<ResponseBodyDTO<Map<String, Object>>>(
				new ResponseBodyDTO<Map<String, Object>>(true, "Maintenance Report History Created Successfully", obj),
				HttpStatus.CREATED);
	}

	@PostMapping("/page")
	public ResponseEntity<Object> getMaintenanceReportHistoryList(
			@RequestParam(value = "_page", required = false) Integer page,
			@RequestParam(value = "_companyUuid", required = false) String companyUuid,
			@RequestParam(value = "_isUuid", required = false) boolean isUuid,
			@RequestParam(value = "_days", required = false) Integer days,
			@RequestParam(value = "_limit", required = false) Integer size,
			@RequestParam(value = "_sort", required = false) String sort,
			@RequestParam(value = "_order", required = false) String order,
			@RequestParam(value = "_filterModelCountFilter", required = false) String filterModelCountFilter,
			@RequestBody Map<String, String> filterValues, HttpServletRequest httpServletRequest) {

		logger.info("Inside getMaintenanceReportHistoryList");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.info("Username : " + jwtUser.getUsername());
		MessageDTO<Page<MaintenanceReportHistoryPayload>> messageDto = new MessageDTO<>(
				"Maintenance Report History Feteched Successfully", true);
		Page<MaintenanceReportHistoryPayload> maitenanceReports = maintenanceReportService
				.getMaintenanceReportHistoryList(getPageable(page - 1, size, sort, order), jwtUser.getUsername(),
						filterValues, filterModelCountFilter, companyUuid, days, sort, isUuid);
		messageDto.setBody(maitenanceReports);
		messageDto.setTotalKey(maitenanceReports.getTotalElements());
		messageDto.setCurrentPage(maitenanceReports.getNumber());
		messageDto.setTotal_pages(maitenanceReports.getTotalPages());

		return new ResponseEntity(messageDto, HttpStatus.OK);

	}

	@PostMapping("/getDetailByUuid")
	public ResponseEntity<Object> getMaintenanceReportHistoryDetailByUuid(@RequestBody List<String> uuidList,
			HttpServletRequest httpServletRequest) {

		logger.info("Inside getMaintenanceReportHistoryList");
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.info("Username : " + jwtUser.getUsername());
		MessageDTO<List<MaintenanceReportHistoryPayload>> messageDto = new MessageDTO<>(
				"Maintenance Report History Feteched Successfully", true);
		List<MaintenanceReportHistoryPayload> maitenanceReports = maintenanceReportService
				.getMaintenanceReportHistoryByUuid(uuidList);
		messageDto.setBody(maitenanceReports);
		return new ResponseEntity(messageDto, HttpStatus.OK);

	}

}
