package com.pct.device.version.payload;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.device.version.dto.StepDTO;
import com.pct.device.version.dto.VersionMigrationDetailDTO;

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
public class CampaignStatsPayload {

	private String uuid;
	
    private String campaignName;
    
    private String campaignStatus;
    
//    private String imeiGroup;
    
    private String description;
    
    private List<VersionMigrationDetailDTO> versionMigrationDetail; 
    
    private Boolean isActive;

    private Boolean isPauseExecution;

    private String isImeiForCustomer;

    private String createdBy;

    private String updatedBy;
	
    private Instant createdAt;

    private Instant updatedAt;
    
    private String customerName;

    private Long noOfImeis ;
    
    private String excludedImeis;
    
    private List<CampaignDeviceDetail> campaignDeviceDetail; 
    
    private CampaignSummary campaignSummary;

    private Boolean excludeNotInstalled;
    
    private Boolean excludeLowBattery;
    
    private Boolean excludeRma;
    private Boolean excludeEol;
	
    private Boolean excludeEngineering;
    private String campaignType;
    private List<StepDTO> stepDTO;
    
    private String initStatus;

}