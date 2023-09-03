package com.pct.device.payload;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;
import com.pct.device.dto.BatteryResponseDTO;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@Setter
@Getter
@NoArgsConstructor
public class AssetsStatusPayload {

	private String imei;
	private String productCode;
	private String productName;
	private String epicorOrderNumber;
	private String can;
	private String uuid;
	private String usage;
	private String lastEvent;
	private String lastReportDate;
	private String lastMaintenanceReportDate;	
	private String lastRawReport;
    private String createdBy;
    private String updatedby;
    private DeviceStatus status;
    private IOTType type;
    private CellularPayload cellularPayload;
    private List<SensorPayLoad> sensorPayload;
    private AssetsDetailPayload assetsDetailPayload;
    private String redisData;
    private String retriveStatus;
    private String purchaseBy;
    private String installedBy;
    private String hardwareId;
    private String hardwareType;
    private Boolean isActive;
    private String installedStatusFlag;
    private String hwIdVersion;
    private String hwVersionRevision;
    private List<BatteryResponseDTO> batteryData;
    private String deviceType;
}
