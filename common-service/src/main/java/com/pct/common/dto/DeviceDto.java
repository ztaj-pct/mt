package com.pct.common.dto;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.constant.AssetCategory;
import com.pct.common.constant.AssetStatus;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;
import com.pct.common.model.Organisation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DeviceDto {
	
	
	private Long Id;
	private String imei;
	private String uuid;
	//private String can;
	//private String status;
	//private String iotType;
	private String macAddress;
//	private String accountNumber;
	private DeviceStatus status;
	private Instant timeOfLastDownload;
	private String gatewayUuid;
	private String productName;
	private String productCode;
	private Instant createdOn;
	private Instant updatedOn;
	private String accountNumber;
	private String lastPerformAction;
	private Boolean isDeleted ;
	private IOTType iotType;
	

	public void setIotType(IOTType iotType) {
		this.iotType = iotType;
	}

	public DeviceDto() {
		super();
	}

	public DeviceDto(String uuid, String imei, String macAddress) {
		super();
		this.imei = imei;
		this.uuid = uuid;
		this.macAddress = macAddress;
	}

	public DeviceDto(String uuid, String imei, String productName, String productCode) {
		super();
		this.imei = imei;
		this.uuid = uuid;
		this.productName = productName;
		this.productCode = productCode;
	}

	public DeviceDto(String uuid, String imei, String macAddress, String accountNumber, DeviceStatus status,
			Instant lastDownloadeTime, String gatewayUuid) {
		super();
		this.imei = imei;
		this.uuid = uuid;
		this.macAddress = macAddress;
		
		this.status = status;
		this.timeOfLastDownload = lastDownloadeTime;
		this.gatewayUuid = gatewayUuid;
	}
	

	
	public DeviceDto(Long id, String uuid, String imei, String macAddress, DeviceStatus status,
			Instant lastDownloadeTime, String productName, String productCode, Instant createdOn,
			Instant updatedOn, String accountNumber, String lastPerformAction, Boolean isDeleted,
			IOTType iotType) {
		super();
		Id = id;
		this.imei = imei;
		this.uuid = uuid;
		this.macAddress = macAddress;
		this.status = status;
		this.timeOfLastDownload = lastDownloadeTime;
		this.productName = productName;
		this.productCode = productCode;
		this.createdOn = createdOn;
		this.updatedOn = updatedOn;
		this.accountNumber = accountNumber;
		this.lastPerformAction = lastPerformAction;
		this.isDeleted = isDeleted;
		this.iotType = iotType;
	}

	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		Id = id;
	}

	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public DeviceStatus getStatus() {
		return status;
	}
	public void setStatus(DeviceStatus status) {
		this.status = status;
	}
	public Instant getLastDownloadeTime() {
		return timeOfLastDownload;
	}
	public void setLastDownloadeTime(Instant lastDownloadeTime) {
		this.timeOfLastDownload = lastDownloadeTime;
	}
	public String getGatewayUuid() {
		return gatewayUuid;
	}
	public void setGatewayUuid(String gatewayUuid) {
		this.gatewayUuid = gatewayUuid;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public Instant getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Instant createdOn) {
		this.createdOn = createdOn;
	}

	public Instant getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Instant updatedOn) {
		this.updatedOn = updatedOn;
	}

    
	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getLastPerformAction() {
		return lastPerformAction;
	}

	public void setLastPerformAction(String lastPerformAction) {
		this.lastPerformAction = lastPerformAction;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	public IOTType getIotType() {
		return iotType;
	}
	
	
	
	
	

	
}
