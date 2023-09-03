package com.pct.installer.service.impl;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pct.common.model.User;
import com.pct.common.model.WorkOrder;
import com.pct.installer.dto.FinishWorkOrderRequest;
import com.pct.installer.dto.WorkOrderDTO;
import com.pct.installer.exception.InstallerException;
import com.pct.installer.repository.IWorkOrderRepository;
import com.pct.installer.service.IInstallAssistService;
import com.pct.installer.util.RestUtils;

@Service
public class InstallAssistService implements IInstallAssistService {

	@Autowired
	private IWorkOrderRepository workOrderRepository;

	@Autowired
	private RestUtils restUtils;

	@Override
	public String saveWorkOrder(WorkOrderDTO workOrderDTO, String userName) {
		User user = restUtils.getUserFromAuthServiceByName(userName);
		WorkOrder workOrder = new WorkOrder();
		workOrder.setWorkOrder(workOrderDTO.getWorkOrder());
		workOrder.setLocationUuid(workOrderDTO.getLocationUuid());
		workOrder.setStartDate(Instant.now());
		workOrder.setServiceDateTime(Instant.now());
		workOrder.setResolutionType(workOrderDTO.getResolutionType());
		workOrder.setValidationTime(workOrderDTO.getValidationTime());

		if (user != null) {
			workOrder.setUser(user);
			if (user.getOrganisation() != null) {
				workOrder.setOrganisation(user.getOrganisation());
			}
		}
		boolean isWorkOrderUuidUnique = false;
		String wordOrderUuid = "";
		while (!isWorkOrderUuidUnique) {
			wordOrderUuid = UUID.randomUUID().toString();
			WorkOrder byUuid = workOrderRepository.findByUuid(wordOrderUuid);
			if (byUuid == null) {
				isWorkOrderUuidUnique = true;
			}
		}
		workOrder.setUuid(wordOrderUuid);
		if (workOrderDTO.getInstallCode() != null) {
			workOrder.setInstallCode(workOrderDTO.getInstallCode());
//			InstallHistory installHistory = installHistoryRepository.findByInstallCode(workOrderDTO.getInstallCode());
//			if (installHistory != null) {
//				workOrder.setInstallHistory(installHistory);
//			}
		}
		workOrder.setStatus("STARTED");
		WorkOrder wor = workOrderRepository.save(workOrder);
		String workOderUuid = wor.getUuid();
		return workOderUuid;
	}

	@Override
	public void finishWorkOrder(FinishWorkOrderRequest workOrderDTO, String userName) {
		if (workOrderDTO == null) {
			throw new InstallerException("Invalid data, Work Order object can not be null");
		}
		if (workOrderDTO.getWorkOrderUuid() == null) {
			throw new InstallerException("Please send valid work order uuid");
		}
		WorkOrder workOrder = workOrderRepository.findWorkOrderByUuid(workOrderDTO.getWorkOrderUuid());
		if (workOrder == null) {
			throw new InstallerException("Work Order not found for this id " + workOrderDTO.getWorkOrderUuid());
		}
		workOrder.setServiceDateTime(Instant.now());
		if (workOrderDTO.getResolutionType() != null) {
			workOrder.setResolutionType(workOrderDTO.getResolutionType());
		}
		if (workOrderDTO.getValidationTime() != null) {
			workOrder.setValidationTime(workOrderDTO.getValidationTime());
		}
		User user = restUtils.getUserFromAuthServiceByName(userName);
		if (user != null) {
			workOrder.setUser(user);
			if (user.getOrganisation() != null) {
				workOrder.setOrganisation(user.getOrganisation());
			}
		}
		workOrder.setStatus("FINISHED");
		if (workOrderDTO.getMaintenanceReportUuid() != null && workOrderDTO.getMaintenanceReportUuid().size() > 0) {
			workOrder.setMaintenanceUuid(workOrderDTO.getMaintenanceReportUuid().toString());
		}
		workOrder.setEndDate(Instant.now());
		workOrderRepository.save(workOrder);
	}

}
