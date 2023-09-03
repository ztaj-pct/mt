package com.pct.common.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "device_maintenance_details", catalog = "pct_device")
@Getter
@Setter
public class DeviceMaintenanceReportDetail implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "imei", columnDefinition = "VARCHAR(50)", nullable = true)
	private String imei;

	@Column(name = "ble_version", columnDefinition = "VARCHAR(255)")
	private String bleVersion;

	@Column(name = "bin_version", columnDefinition = "VARCHAR(255)")
	private String binVersion;

	@Column(name = "mcu_version", columnDefinition = "VARCHAR(255)")
	private String mcuVersion;

	@Column(name = "app_version", columnDefinition = "VARCHAR(255)")
	private String appVersion;

	@Column(name = "config1_name", columnDefinition = "VARCHAR(255)")
	private String config1Name;

	@Column(name = "config2_name", columnDefinition = "VARCHAR(255)")
	private String config2Name;

	@Column(name = "config3_name", columnDefinition = "VARCHAR(255)")
	private String config3Name;

	@Column(name = "config4_name", columnDefinition = "VARCHAR(255)")
	private String config4Name;

	@Column(name = "config1CRC", columnDefinition = "VARCHAR(255)")
	private String config1CRC;

	@Column(name = "config2CRC", columnDefinition = "VARCHAR(255)")
	private String config2CRC;

	@Column(name = "config3CRC", columnDefinition = "VARCHAR(255)")
	private String config3CRC;

	@Column(name = "config4CRC", columnDefinition = "VARCHAR(255)")
	private String config4CRC;

	@Column(name = "devuser_cfg_name", columnDefinition = "VARCHAR(255)")
	private String devuserCfgName;

	@Column(name = "devuser_cfg_value", columnDefinition = "VARCHAR(255)")
	private String devuserCfgValue;

	@Column(name = "latest_maintenance_report")
	private Date latestMaintenanceReport;

	@Column(name = "event_id")
	private int eventId;

	@Column(name = "event_type")
	private String eventType;

	@Column(name = "hardware_id")
	private String hardwareId;

	@Column(name = "hardware_type")
	private String hardwareType;

	@JsonIgnore
	@OneToOne(mappedBy = "deviceMaintenanceDetails")
	private Device device;

	@Column(name = "hw_id_version")
	private String hwIdVersion;

	@Column(name = "hw_version_revision")
	private String hwVersionRevision;

	@Column(name = "installed_status_flag")
	private String installedStatusFlag;

	@Column(name = "usage_status", columnDefinition = "VARCHAR(255)")
	private String usageStatus;

}
