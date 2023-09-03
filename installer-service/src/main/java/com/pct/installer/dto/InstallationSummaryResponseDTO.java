package com.pct.installer.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class InstallationSummaryResponseDTO {
	
	    private String assetUuid;
	    private String deviceUuid;
	    private boolean filter;
	    private String installCode;
	    private String assetId;
	    private String status;
	    private String productCode;
	    private String productName;
	    private String deviceId;
	    private String installed;
	    private String cargoSensor;
	    //private String pctCargoSensor;
	    private String cargoCameraSensor;
	    private String doorSensor;
	    private String ABSSensor;
	    private String atisSensor;
	    private String lightSentry;
	    private String tpms;
	    private String wheelEnd;
	    private String airTank;
	    private String regulator;
	    private String reciever;
	    private String installerName;
	    private String installerCompany;
	    private Long userId;
	    private String batteryVoltage;
	    private String batteryStatus;
	    private String primaryVoltage;
	    private String primaryStatus;
	    private String secondaryVoltage;
	    private String secondaryStatus;
	    private String doorMacAddress;
	    private String doorType;
	    private String lampCheckAtis;
	    private String lampCheckAtisMac;
	    private String tpmsLof;
	    private String tpmsLif;
	    private String tpmsRof;
	    private String tpmsRif;
	    private String tpmsLor;
	    private String tpmsLir;
	    private String tpmsRor;
	    private String tpmsRir;
	    
	    private String tpmsLofStatus;
	    private String tpmsLifStatus;
	    private String tpmsRofStatus;
	    private String tpmsRifStatus;
	    private String tpmsLorStatus;
	    private String tpmsLirStatus;
	    private String tpmsRorStatus;
	    private String tpmsRirStatus;
	    
	    private String tpmsLofTemperature;
	    private String tpmsLifTemperature;
	    private String tpmsRofTemperature;
	    private String tpmsRifTemperature;
	    private String tpmsLorTemperature;
	    private String tpmsLirTemperature;
	    private String tpmsRorTemperature;
	    private String tpmsRirTemperature;
	    
	    private String tpmsLofPressure;
	    private String tpmsLifPressure;
	    private String tpmsRofPressure;
	    private String tpmsRifPressure;
	    private String tpmsLorPressure;
	    private String tpmsLirPressure;
	    private String tpmsRorPressure;
	    private String tpmsRirPressure;
	    
	    private String airTankId;
	    private String regulatorId;
	    
	    private String airTankTemperature;
	    private String regulatorTemperature;
	    
	    private String airTankPressure;
	    private String regulatorPressure;
	    
	    private String appVersion;
		/*private String microSpReceiver;
		private String microSpAirTank;
		private String microSpRegulator;*/
	    private Instant createdAt;
	    private Instant updatedAt;
	    private Instant dateStarted;
	    
	    private String cargoCameraMac;
	    private String maxonMaxLinkMac;
	    private String tankSaverMac;
	    private String maxonMaxLink; // 77-DB40
	    private String tankSaver; // 77-S239
	    private String organisationName; 
        private Long orgId;   
	    private String userName; 
	    
	   
	  
	    
	    
}
