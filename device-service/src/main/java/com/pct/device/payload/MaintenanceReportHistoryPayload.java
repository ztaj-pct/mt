package com.pct.device.payload;

import java.time.Instant;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.model.Asset;
import com.pct.common.model.Device;
import com.pct.common.model.Organisation;
import com.pct.common.model.User;

import lombok.Data;

@Data
//@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MaintenanceReportHistoryPayload {

	private Long id;
	private Instant createdDate;
	private Instant serviceTime;
	private String pstServiceTime;
	private String assetId;
	private String assetUuId;
	private String deviceId;
	private String sensorType;
	private String oldSensorId;
	private String newSensorId;
	private String validationTime;
	private String resolutionType;
	private String workOrder;
	private String serviceVendorName;
	private String technician;
	private String maintenanceLocation;
	private String sensorName;
	private String position;
	private String status;
	private String reading;
	private String macAddress;
	private String oldMacAddress;
	
	
}
