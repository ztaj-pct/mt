package com.pct.device.version.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pct.common.model.User;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@ToString
@Table(name = "package")

public class Package implements Serializable{


    @Column(name = "uuid")
    String uuid;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "package_id")
	Long packageId;

	@Column(name = "package_name")
	String packageName;
	
	@Column(name = "bin_version")
	String binVersion;
	

	@Column(name = "app_version")
	String appVersion;
	
	
	@Column(name = "mcu_version")
	String mcuVersion;
	

	
	@Column(name = "ble_version")
	String bleVersion;
	
	
	
	@Column(name = "config1")
	String config1;
	
	@Column(name = "config2")
	String config2;
	
	@Column(name = "config3")
	String config3;
	
	@Column(name = "config4")
	String config4;
	

	@Column(name = "config1_crc")
	String config1Crc;
	

	@Column(name = "config2_crc")
	String config2Crc;
	

	@Column(name = "config3_crc")
	String config3Crc;
	

	@Column(name = "config4_crc")
	String config4Crc;
	

	@Column(name = "is_deleted")
	Boolean isDeleted;
	
	
	@JsonIgnoreProperties("role")
    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "uuid")
    User createdBy;

	
	@JsonIgnoreProperties("role")
    @ManyToOne
    @JoinColumn(name = "updated_by", referencedColumnName = "uuid")
    User updatedBy;
    
	
	@Column(name = "created_at")
	Instant createdAt;
	
	@Column(name = "updated_at")
	Instant updatedAt;
	
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
 	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Long getPackageId() {
		return packageId;
	}

	public void setPackageId(Long packageId) {
		this.packageId = packageId;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getBinVersion() {
		return binVersion;
	}

	public void setBinVersion(String binVersion) {
		this.binVersion = binVersion;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getMcuVersion() {
		return mcuVersion;
	}

	public void setMcuVersion(String mcuVersion) {
		this.mcuVersion = mcuVersion;
	}

	public String getBleVersion() {
		return bleVersion;
	}

	public void setBleVersion(String bleVersion) {
		this.bleVersion = bleVersion;
	}

	public String getConfig1() {
		return config1;
	}

	public void setConfig1(String config1) {
		this.config1 = config1;
	}

	public String getConfig2() {
		return config2;
	}

	public void setConfig2(String config2) {
		this.config2 = config2;
	}

	public String getConfig3() {
		return config3;
	}

	public void setConfig3(String config3) {
		this.config3 = config3;
	}

	public String getConfig4() {
		return config4;
	}

	public void setConfig4(String config4) {
		this.config4 = config4;
	}

	public String getConfig1Crc() {
		return config1Crc;
	}

	public void setConfig1Crc(String config1Crc) {
		this.config1Crc = config1Crc;
	}

	public String getConfig2Crc() {
		return config2Crc;
	}

	public void setConfig2Crc(String config2Crc) {
		this.config2Crc = config2Crc;
	}

	public String getConfig3Crc() {
		return config3Crc;
	}

	public void setConfig3Crc(String config3Crc) {
		this.config3Crc = config3Crc;
	}

	public String getConfig4Crc() {
		return config4Crc;
	}

	public void setConfig4Crc(String config4Crc) {
		this.config4Crc = config4Crc;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public User getCreatedBy() {
		return createdBy;
	}
	@JsonIgnore
	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}
	@JsonIgnore
	public User getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(User updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getMicrospApp() {
		return microspApp;
	}

	public void setMicrospApp(String microspApp) {
		this.microspApp = microspApp;
	}

	public String getMicrospMcu() {
		return microspMcu;
	}

	public void setMicrospMcu(String microspMcu) {
		this.microspMcu = microspMcu;
	}

	public String getCargoMaxbotixHardware() {
		return cargoMaxbotixHardware;
	}

	public void setCargoMaxbotixHardware(String cargoMaxbotixHardware) {
		this.cargoMaxbotixHardware = cargoMaxbotixHardware;
	}

	public String getCargoMaxbotixFirmware() {
		return cargoMaxbotixFirmware;
	}

	public void setCargoMaxbotixFirmware(String cargoMaxbotixFirmware) {
		this.cargoMaxbotixFirmware = cargoMaxbotixFirmware;
	}

	public String getCargoRiotHardware() {
		return cargoRiotHardware;
	}

	public void setCargoRiotHardware(String cargoRiotHardware) {
		this.cargoRiotHardware = cargoRiotHardware;
	}

	public String getCargoRiotFirmware() {
		return cargoRiotFirmware;
	}

	public void setCargoRiotFirmware(String cargoRiotFirmware) {
		this.cargoRiotFirmware = cargoRiotFirmware;
	}

	public String getLiteSentryHardware() {
		return liteSentryHardware;
	}

	public void setLiteSentryHardware(String liteSentryHardware) {
		this.liteSentryHardware = liteSentryHardware;
	}

	public String getLiteSentryApp() {
		return liteSentryApp;
	}

	public void setLiteSentryApp(String liteSentryApp) {
		this.liteSentryApp = liteSentryApp;
	}

	public String getLiteSentryBoot() {
		return liteSentryBoot;
	}

	public void setLiteSentryBoot(String liteSentryBoot) {
		this.liteSentryBoot = liteSentryBoot;
	}
	
}
