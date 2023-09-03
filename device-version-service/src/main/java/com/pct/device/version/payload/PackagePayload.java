package com.pct.device.version.payload;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.model.User;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author dhruv
 *
 */
@Data
@NoArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PackagePayload {

	private String uuid;
	
    private String packageName;

    private String binVersion;

    private String appVersion;

    private String mcuVersion;
	
    private String bleVersion;
    
    private String config1;
	
    private String config2;
	
    private String config3;
	
    private String config4;
    
    private String config1Crc;
	
    private String config2Crc;
	
    private String config3Crc;
	
    private String config4Crc;
    
    private Boolean isDeleted;
    
    private Boolean isUsedInCampaign;

    private String createdBy;

    private String updatedBy;
	
    private Instant createdAt;

    private Instant updatedAt;
    
    private String deviceType;
    
    private String liteSentryHardware;
	
    private String liteSentryApp;
	
    private String liteSentryBoot;
 	
    private String microspMcu;
    
    private String microspApp;
	
    private String cargoMaxbotixHardware;
	
    private String cargoMaxbotixFirmware;
	
    private String cargoRiotHardware;
	
    private String cargoRiotFirmware;

}
