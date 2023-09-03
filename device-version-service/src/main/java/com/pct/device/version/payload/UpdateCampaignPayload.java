package com.pct.device.version.payload;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.device.version.dto.CampaignUpdateVersionDetailDTO;
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
public class UpdateCampaignPayload {

	private String uuid;
	
    private String campaignName;
    
    private String campaignStatus;
    
    private String imeiList ;
    
    private String notEligibleDevices;
    
    private String description;
    
    private String campaignPriorityForDevice;
    
    private List<CampaignUpdateVersionDetailDTO> versionMigrationDetail; 
    
    private Boolean isDeleted;
    
    private Boolean isActive;

    private String createdBy;

    private String updatedBy;
	
    private Instant createdAt;

    private Instant updatedAt;
    
    private String customerName;
    
    private String isImeiForCustomer;
    
    private Boolean isPauseExecution;
    
    private Long noOfImeis ;
    
    private transient String tooltip;

    private Boolean excludeNotInstalled;
    
    private Boolean excludeLowBattery;
    
    private Boolean excludeRma;
    
    private Boolean excludeEol;
	
    private Boolean excludeEngineering;
    
    private Boolean isReplace;
    
    private String deviceType;
    
    private List<CampaignDeviceDetail> campaignDeviceDetail; 
    
    private List<String> imeisToBeRemoved;
    
    private String excludedImeis;
    
    private String deviceStatusForCampaign;


}
