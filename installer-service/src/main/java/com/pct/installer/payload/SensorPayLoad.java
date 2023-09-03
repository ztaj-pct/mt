package com.pct.installer.payload;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;

import lombok.Data;
import lombok.NoArgsConstructor;


@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
public class SensorPayLoad {

	private String productCode;
	private String productName;
	private String bleVersion;
	private String epicorOrderNumber;
	private String can;
	private String son;
	private String macAddress;
	private String uuid;
	private String createdBy;
    private String updatedBy;
    private DeviceStatus status;
    private IOTType type;
    private List<SensorSubDetailPayload> sensorSubDetail; 
    

}
