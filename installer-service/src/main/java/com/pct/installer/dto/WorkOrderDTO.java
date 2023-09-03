package com.pct.installer.dto;

import java.time.Instant;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

//@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class WorkOrderDTO {

	private String workOrder;
	private String locationUuid;
//	private Instant startDate;
//	private Instant endDate;
	private String installCode;
	private String validationTime;
	private String resolutionType;
}
