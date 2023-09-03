package com.pct.device.version.payload;

import java.time.Instant;
import java.util.List;

import javax.persistence.Column;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.device.version.dto.VersionMigrationDetailDTO;
import com.pct.device.version.model.Grouping;

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
public class CampaignPayload {

	private String uuid;
	
    private String campaignName;
    
    private String campaignStatus;
    
    private String imeiGroup;
    
    private Long imeiCount;
    
    private String description;
    
    private String campaignPriorityForDevice;
    
    private List<VersionMigrationDetailDTO> versionMigrationDetail; 
    
    private Boolean isDeleted;
    
    private Boolean isActive;

    private String createdBy;

    private String updatedBy;
	
    private Instant createdAt;

    private Instant updatedAt;
    
    private String customerName;
    
    private String imeiList ;
    
    private Boolean isImeiForCustomer;
    
    private Boolean isPauseExecution;
    
    private Long noOfImeis ;

    private Boolean excludeNotInstalled;
    
    private Boolean excludeLowBattery;
    
    private Boolean excludeRma;
    private Boolean excludeEol;
    
	private Boolean excludeEngineering;
    
}