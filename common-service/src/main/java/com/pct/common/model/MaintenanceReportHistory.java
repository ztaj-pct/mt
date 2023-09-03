package com.pct.common.model;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "maintenance_report_history", catalog = "pct_device")
public class MaintenanceReportHistory implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "uuid")
	private String uuid;
	
	@Column(name = "old_sensor_id")
	private String oldSensorId;
	
	@Column(name = "new_sensor_id")
	private String newSensorId;
	
	@Column(name = "sensor_type")
	private String sensorType;
	
	@Column(name = "work_order")
	private String workOrder;

	@Column(name = "resolution_type")
	private String resolutionType;
	
	@Column(name = "validation_time")
	private String validationTime;
	
	@Column(name = "maintenance_location")
	private String maintenanceLocation;
	
	@Column(name = "service_date_time")
	private Instant serviceDateTime;
	
	@Column(name = "created_date")
	private Instant createdDate;
	
	@ManyToOne
    @JoinColumn(name = "technician_user_uuid", referencedColumnName = "uuid")
	private User user;
	
	@ManyToOne
    @JoinColumn(name = "device_uuid", referencedColumnName = "uuid")
	private Device device;
	
	@ManyToOne
    @JoinColumn(name = "asset_uuid", referencedColumnName = "uuid")
	private Asset asset;
	
	@ManyToOne
	@JoinColumn(name = "service_vendor_org_uuid", referencedColumnName = "uuid")
	private Organisation organisation;

	@Column(name = "mac_address")
	private String macAddress;
	
	@Column(name = "old_mac_address")
	private String oldMacAddress;
	

	@Column(name = "sensor_uuid")
	private String sensorUuid;

	@Column(name = "position")
	private String position;

	

}
