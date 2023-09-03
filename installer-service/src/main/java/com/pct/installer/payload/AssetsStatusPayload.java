package com.pct.installer.payload;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;

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
	private String quantityShipped;
	private String appVersion;
	private String mcuVersion;
	private String binVersion;
	private String bleVersion;
	private String other1Version;
	private String other2Version;
	private String config1;
	private String config2;
	private String config3;
	private String config4;
	private String epicorOrderNumber;
	private String can;
	private String son;
	private String macAddress;
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
}
