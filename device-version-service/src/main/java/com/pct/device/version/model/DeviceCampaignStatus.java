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
@Table(name = "device_campaign_status")
@Getter
@Setter
public class DeviceCampaignStatus {

	@Column(name = "status")
	@Enumerated(value = EnumType.STRING)
	CampaignStepDeviceStatus status;

	@Column(name = "start_execution_time")
	Instant startExecutionTime;

	@Column(name = "uuid")
	String uuid;

	@Column(name = "device_id")
	String deviceId;

	@ManyToOne
	@JoinColumn(name = "step_uuid", referencedColumnName = "uuid")
	CampaignStep campaignStep;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	Long id;

	@ManyToOne
	@JoinColumn(name = "campaign_uuid", referencedColumnName = "uuid")
	Campaign campaign;

	@Column(name = "updated_at")
	Instant updatedAt;

	@Column(name = "created_at")
	Instant createdAt;

	@Column(name = "last_reported_at")
	Instant lastReportedAt;

	@Column(name = "last_step_uuid")
	String lastStepUUID;
	
	@Column(name = "last_completed_step_uuid")
	String lastCompletedStepUUID;
	
	@Column(name = "off_on_path")
	String offOnPath;
	@Column(name = "eligibility")
	String eligibility;
	@Column(name = "running_status")
	String runningStatus;
	@Column(name = "problem")
	String problem;
	@Column(name = "on_hold")
	String onHold;
	@Column(name = "comment")
	String comment;
	@Column(name = "installed_flag")
	String installedFlag;
	@Column(name = "baslLine_match")
	String baseLineMatch;
	@Column(name = "problem_comment")
	String problemComment;
	
	@Column(name = "customer_name")
	String customerName;
	@Column(name = "customer_id")
	String customerId;

	@Column(name = "last_step_order_number")
	long lastStepOrderNumber;

	@Column(name = "last_step_execution_date")
	String lastStepExecutionDate;
}
