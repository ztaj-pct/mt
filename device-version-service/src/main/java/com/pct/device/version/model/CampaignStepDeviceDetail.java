package com.pct.device.version.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.pct.common.model.User;
import com.pct.device.version.constant.CampaignStatus;
import com.pct.device.version.constant.CampaignStepDeviceStatus;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "campaign_step_device_detail")
@Getter
@Setter
public class CampaignStepDeviceDetail {

    @Column(name = "status")
	@Enumerated(value = EnumType.STRING)
	CampaignStepDeviceStatus status;
	
	@Column(name = "start_execution_time")
	Instant startExecutionTime;
	
	@Column(name = "stop_execution_time")
	Instant stopExecutionTime;

    @Column(name = "uuid")
    String uuid;
    
    @Column(name = "device_id")
    String deviceId;
    
    @ManyToOne
    @JoinColumn(name = "step_uuid", referencedColumnName = "uuid")
    CampaignStep campaignStep;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "campaign_step_run_id")
	Long campaignStepRunId;

    @ManyToOne
    @JoinColumn(name = "campaign_uuid", referencedColumnName = "uuid")
    Campaign campaign;
    
    @Column(name = "updated_at")
	Instant updatedAt;
	
	@ManyToOne
	@JoinColumn(name = "updated_by", referencedColumnName = "uuid")
	User updatedBy;

	@Column(name = "problem_email", columnDefinition = "boolean default false")
	Boolean problemEmail = false;
}
