package com.pct.installer.dto;

import java.util.Set;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.model.Device;
import com.pct.common.model.WorkOrder;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class InstallationDetailResponseDTO {
	    private String assetUuid;
	    private String assetStatus;
	    private String installCode;
	    private String assetDetails;
	    private String gatewayDetails;
	    private String installerName;
	    private String installerPhone;
	    private String deviceId;
	    private String installedDate;
	    private String lastUpdated;
	    private String dateStarted;
	    private String appVersion;
	    private String lastMaintenanceReportDate;
	    private AssetDetailResponseDTO assetDetailsResposne;
	    private Set<SensorResponseDTO> sensorDetails;
	    private List<BatteryResponseDTO> batteryData;
	    private List<LoggedIssueResponseDTO> loggedIssues;
	    private List<FinishWorkOrderDTO> finishWorkOrder;
	    private String qaDateAndTime;
	    private String configName;
	    private String qaStatus;

}
