package com.pct.device.version.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@Data
@NoArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CampaignStatsPayloadList {


    private String uuid;

    private String campaignName;

    private String campaignStatus;

//    private String imeiGroup;

    private String description;


    private Boolean isActive;

    private Boolean isPauseExecution;

    private Boolean isImeiForCustomer;

    private String createdBy;

    private String updatedBy;

    private Instant createdAt;

    private Instant updatedAt;

    private String customerName;

    private Long noOfImeis ;

    private Boolean excludeNotInstalled;

    private Boolean excludeLowBattery;
    
    private Boolean excludeRma;
    private Boolean excludeEol;
	private Boolean excludeEngineering;
	
    private String lastCommandDays;
    private String daysRunning;


    private Long totalGateways;
    private Long notStarted;
    private Long inProgress;
    private Long completed;
    private Long onHold;
    private Long notEligible;
    private Long eligible;
    private Long problemCount;
    private Long offPath;
	private Long onPath;
}
