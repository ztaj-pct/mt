package com.pct.device.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;
import com.pct.common.model.User;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.time.Instant;
import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
public class GatewaySensorPayload {

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
    private String createdBy;
    private String updatedby;
    private DeviceStatus status;
    private String hardwareId;
    private String hardwareType;
    private IOTType type;
    private CellularPayload cellularPayload;
    private List<SensorPayLoad> sensorPayload;
    private List<PeripheralVesion> peripheralVersion;
    private String redisData;
    
}
