package com.pct.installer.dto;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

//@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class FinishWorkOrderRequest {

	private String workOrderUuid;
	private List<String> maintenanceReportUuid;
	private String validationTime;
	private String resolutionType;
	
//	@JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss")
	//private Instant finishTime;
}
