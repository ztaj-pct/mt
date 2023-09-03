package com.pct.installer.service;

import com.pct.installer.dto.FinishWorkOrderRequest;
import com.pct.installer.dto.WorkOrderDTO;

public interface IInstallAssistService {
	
	public String saveWorkOrder(WorkOrderDTO workOrderDTO,String userName);
	
	public void finishWorkOrder(FinishWorkOrderRequest workOrderDTO,String userName);

}
