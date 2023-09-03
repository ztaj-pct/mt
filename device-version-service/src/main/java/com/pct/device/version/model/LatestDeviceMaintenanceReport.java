package com.pct.device.version.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "latest_device_maintenance_report", catalog = "pct_campaign")
public class LatestDeviceMaintenanceReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "latest_device_maintenance_report_id")
    private Long latestDeviceMaintenanceReportId;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "extender_version")
    private String extenderVersion;
    
    @Column(name = "ble_version")
    private String bleVersion;

    
    @Column(name = "sw_version_application")
    private String swVersionApplication;

    @Column(name = "sw_version_baseband")
    private String swVersionBaseband;

    @Column(name = "timestamp_received_pst")
    private Timestamp timestampReceivedPST;
    
    @Column(name = "timestamp_updated_pst")
    private Timestamp timestampUpdatedPST;

    @Column(name = "config1")
    private String config1;

    @Column(name = "config2")
    private String config2;

    @Column(name = "config3")
    private String config3;

    @Column(name = "config4")
    private String config4;

    @Column(name = "config5")
    private String config5;

    @Column(name = "config1_crc")
    private String config1Crc;

    @Column(name = "config2_crc")
    private String config2Crc;

    @Column(name = "config3_crc")
    private String config3Crc;

    @Column(name = "config4_crc")
    private String config4Crc;

    @Column(name = "config5_crc")
    private String config5Crc;
    
    @Column(name = "device_type")
	String deviceType;
	
	@Column(name = "lite_sentry_hardware")
	String liteSentryHardware;
	
	@Column(name = "lite_sentry_app")
	String liteSentryApp;
	
	@Column(name = "lite_sentry_boot")
	String liteSentryBoot;
	
	@Column(name = "microsp_app")
	String microspApp;
	
	@Column(name = "microsp_mcu")
	String microspMcu;
	
	@Column(name = "cargo_maxbotix_hardware")
	String cargoMaxbotixHardware;
	
	@Column(name = "cargo_maxbotix_firmware")
	String cargoMaxbotixFirmware;
	
	@Column(name = "cargo_riot_hardware")
	String cargoRiotHardware;
	
	@Column(name = "cargo_riot_firmware")
	String cargoRiotFirmware;
    
	@Column(name = "raw_report")
    private String rawReport;
	
	@Column(name = "report_id")
    private String reportId;
    
}