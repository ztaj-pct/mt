package com.pct.device.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.model.User;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.time.Instant;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
public class SensorDetailPayLoad {

	private String productCode;
	private String productName;
	private String bleVersion;
	private String epicorOrderNumber;
	private String can;
	private String son;
	private String macAddress;
	private String uuid;
	
	 //new sensor list
    private String lampCheckABS; // 77-S191
    private String pctCargoSensor; // 77-S180
    private String microSpAirTank; // 77-S202
    private String microSpATISRegulator; // 77-S203
    private String microSpWiredReceiver; // 77-S206
    
    private String microSpTPMSFourIntoTwo; // 77-S177 4x2
    private String microSpTPMSFourIntoOne; // 77-S177 4x1
    private String microSpTPMSTwoIntoTwo; // 77-S177 2x2
    
    
    private String microSpTPMSOuter; // 77-S199
    private String microSpTPMSInner; // 77-S198
    // 'Wheel End Sensor', 'f6824880-9ed6-4613-9dd6-4634a6ec08e5', '16', '77-S183'
    // 'LampCheck ATIS', '761c38e0-7cfe-4876-a914-c75f4674debf', '35', '77-S187'
    
    private String pctCargoCameraG1; // 77-S111
    private String pctCargoCameraG2; // 77-S225
    private String pctCargoCameraG3; // 77-S226
    
    private String maxonMaxLink; // 77-DB40
    private String tankSaver; // 77-S239
    
    private Instant createdAt;
    private Instant updatedAt;

}
