package com.pct.device.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pct.common.model.MaintenanceReportHistory;
import com.pct.device.dto.MaintenanceReportDTO;
import com.pct.device.payload.MaintenanceReportHistoryPayload;

public interface IMaintenanceReportService {
	
	public MaintenanceReportHistory createMaintenaceRportHistory(MaintenanceReportDTO maintenanceReportDTO, String userName);

	Page<MaintenanceReportHistoryPayload> getMaintenanceReportHistoryList(Pageable pageable, String userName, Map<String, String> filterValues,
			String filterModelCountFilter, String companyUuid, Integer days, String sort,boolean isUuid);
	
	List<MaintenanceReportHistoryPayload> getMaintenanceReportHistoryByUuid(List<String> uuidList);
}