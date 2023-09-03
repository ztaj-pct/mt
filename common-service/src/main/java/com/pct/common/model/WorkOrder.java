package com.pct.common.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "work_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkOrder extends DateAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "uuid")
	private String uuid;

	private String workOrder;

	private String locationUuid;

	private Instant startDate;

	private Instant endDate;

//	@ManyToOne
//	@JoinColumn(name = "install_history", referencedColumnName = "uuid")
//	private InstallHistory installHistory;

	private String installCode;
	
	private String maintenanceUuid;
	
	private String status;
	
	@Column(name = "resolution_type")
	private String resolutionType;
	
	@Column(name = "validation_time")
	private String validationTime;
	
	@Column(name = "service_date_time")
	private Instant serviceDateTime;
	
	@ManyToOne
    @JoinColumn(name = "technician_user_uuid", referencedColumnName = "uuid")
	private User user;
	
	@ManyToOne
	@JoinColumn(name = "service_vendor_org_uuid", referencedColumnName = "uuid")
	private Organisation organisation;
}
