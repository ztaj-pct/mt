package com.pct.device.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceHealthDTO {
	
	String assetId;
	String gatewayId;
	String overallStatus;
	String gpsLockStatus;
	String bluePowerStatus;
	String brownPowerStatus;
	String dateInstalled;
	String deviceConfig;
	String lastEvent;
	String lastEventRtc;
	String currentGpsStatus;
	String currentBatteryPowerV;
	String currentMainPowerV;
	String currentAltPowerV;
	String maintenanceEventSeen;

}
