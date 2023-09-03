package com.pct.installer.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class FinishWorkOrderDTO {

	private Long id;

	private String uuid;

	private String workOrder;

	private String locationUuid;

	private String startDate;

	private String endDate;

	private String installCode;
	
	private String maintenanceUuid;
	
	private String status;
	
	private String validationTime;
	
	private String resolutionType;
	
	private String techName;
	
	private String serviceVendorName;
	
	private String serviceDateTime;
}
