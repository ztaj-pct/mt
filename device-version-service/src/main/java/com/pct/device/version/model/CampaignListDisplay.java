package com.pct.device.version.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "campaign_list_display")
@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CampaignListDisplay {

	@Id
	@Column(name = "campaign_list_item_id")
	Long CampaignListItemId;

	@Column(name = "uuid")
	private String uuid;

	@Column(name = "campaign_name")
    private String campaignName;

	@Column(name = "description")
    private String description;

	@Column(name = "campaign_status")
    private String campaignStatus;

	@Column(name = "last_command_days")
    private String lastCommandDays;

	@Column(name = "problem_count")
    private Long problemCount;

	@Column(name = "completed")
    private Long completed;
    
	@Column(name = "eligible")
    private Long eligible;
	
	@Column(name = "days_running")
    private String daysRunning;
    
	@Column(name = "created_by")
    private String createdBy;

	@Column(name = "updated_by")
    private String updatedBy;

	@Column(name = "created_at")
    private Instant createdAt;
    
	@Column(name = "customer_name")
    private String customerName;
	
	@Column(name = "list_updated_at")
    private String listUpdatedAt;
	
	@Column(name = "in_progress")
    private Long inProgress;
	
	@Column(name = "not_started")
    private Long  notStarted;
	
	@Column(name = "campaign_started_date")
    private Instant campaignStartDate;
	
	@Column(name = "off_path")
	private Long offPath;

	@Column(name = "on_path")
	private Long onPath;

	@Column(name = "not_installed")
	private Long notInstalled;
	
	@Column(name = "device_type")
	private String deviceType;
}                               