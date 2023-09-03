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
import com.pct.device.version.constant.CampaignStepDeviceStatus;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "device_status")
@Getter
@Setter
public class DeviceStatus {

    @Column(name = "running_status")
	@Enumerated(value = EnumType.STRING)
	CampaignStepDeviceStatus runningStatus;
	
    @Column(name = "uuid")
    String uuid;
    
    @Column(name = "device_id")
    String deviceId;
    
    @ManyToOne
    @JoinColumn(name = "step_uuid", referencedColumnName = "uuid")
    CampaignStep currentCampaignStep;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "device_status_id")
	Long deviceStatusId;

    @ManyToOne
    @JoinColumn(name = "campaign_uuid", referencedColumnName = "uuid")
    Campaign campaign;
    
    @Column(name = "updated_at")
	Instant updatedAt;
	
    @Column(name = "created_at")
   	Instant createdAt;
    
    @Column(name = "last_reported_time")
	Instant lastReportedTime;
    
    @Column(name = "last_report")
   	String lastReport;
	
}
