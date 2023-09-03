package com.pct.device.version.payload;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

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
public class SaveCampaignRequest {

    private String campaignName;
    
    private String customerName;
    private String imeiList ;
    private String notEligibleDevices;
    private Boolean isImeiForCustomer;
    
    private Boolean isPauseExecution;
    private Long noOfImeis ;
    
    private List<VersionMigrationDetail> versionMigrationDetail;
    private String description;
    private String priority;
    private transient String tooltip;
    private String deviceType;
    private Boolean excludeNotInstalled;
    
    private Boolean excludeLowBattery;
    
    private Boolean excludeRma;
    private Boolean excludeEol;
	
    private Boolean excludeEngineering;
    private String excludedImeis;
    private String campaignType;
}
