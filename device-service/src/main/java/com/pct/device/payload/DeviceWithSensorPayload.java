package com.pct.device.payload;

import java.time.Instant;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
public class DeviceWithSensorPayload {

	private String uuid;
    private String productName;
    private String productCode;
    private String imei;
    private String companyName;
    private String accountNumber;
    private String status;
    private String createdBy;
    private String updatedBy;	
    private String orderNo;
    private String cargoSensor;
    private String doorSensor;
    private String absSensor;
    private String atisSensor;
    private String lightSentry;
    private String tpms;
    private String wheelEnd;
    private String airTank;
    private String regulator;
    private String microSpTransceiver;
    private String lampCheckAtis;
    private String gatewayType;
    
    //new sensor list
    private String lampCheckABS; // 77-S191
    private String pctCargoSensor; // 77-S180
    private String microSpAirTank; // 77-S202
    private String microSpATISRegulator; // 77-S203
    private String microSpWiredReceiver; // 77-S206
    private String microSpTPMS; // 77-S177
    private String microSpTPMS2X2; // 77-S177 2x2 – MicroSP TPMS Sensors
    private String microSpTPMS4X1; // 77-S177 4x1 – MicroSP TPMS Sensors
    private String microSpTPMS4X2; // 77-S177 4x2 – MicroSP TPMS Sensors
    private String microSpTPMSOuter; // 77-S199
    private String microSpTPMSInner; // 77-S198
    // 'Wheel End Sensor', 'f6824880-9ed6-4613-9dd6-4634a6ec08e5', '16', '77-S183'
    // 'LampCheck ATIS', '761c38e0-7cfe-4876-a914-c75f4674debf', '35', '77-S187'
    
    private String pctCargoCameraG1; // 77-S111
    private String pctCargoCameraG2; // 77-S225
    private String pctCargoCameraG3; // 77-S226
    
    private Instant createdAt;
    private Instant updatedAt;

}
