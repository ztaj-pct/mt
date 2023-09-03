package com.pct.device.version.model;

import com.pct.common.model.User;
import com.pct.device.version.constant.CampaignStatus;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "campaign")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Campaign implements Serializable{
	private static final long serialVersionUID = 1L;
	@Column(name = "campaign_status")
	@Enumerated(value = EnumType.STRING)
	CampaignStatus campaignStatus;
	
	@Column(name = "campaign_name")
	String campaignName;
	
    @ManyToOne
    @JoinColumn(name = "grouping_uuid", referencedColumnName = "uuid")
	Grouping group;
	
	@Column(name = "created_at")
	Instant createdAt;
	
	@Column(name = "updated_at")
	Instant updatedAt;
	
	@ManyToOne
	@JoinColumn(name = "created_by", referencedColumnName = "uuid")
	User createdBy;

	@ManyToOne
	@JoinColumn(name = "updated_by", referencedColumnName = "uuid")
	User updatedBy;

	@Column(name = "campaign_start_date")
	Instant campaignStartDate;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "campaign_id")
	Long campaignId;
	
    @Column(name = "uuid")
    String uuid;
    
    @Column(name = "description")
    String description;
    
    @Column(name = "pause_limit")
    Long pauseLimit;
    
    @Column(name = "pause_execution")
    Boolean pauseExecution;
    
	@Column(name = "is_deleted")
	Boolean isDeleted;
    
	@Column(name = "is_active")
	Boolean isActive;
	
	@Column(name = "exclude_not_installed")
	Boolean excludeNotInstalled;
    
	@Column(name = "exclude_low_battery")
	Boolean excludeLowBattery;
	
	@Column(name = "exclude_engineering")
	Boolean excludeEngineering;
	
	@Column(name = "exclude_rma")
	Boolean excludeRma;
	
	@Column(name = "exclude_eol")
	Boolean excludeEol;
	
	@Column(name = "device_type")
	String deviceType;
	 
	@Column(name = "customer_name")
	String customerName;

	@Column(name = "campaign_type")
	String campaignType;
	
	@Column(name = "init_status")
	String initStatus;
}