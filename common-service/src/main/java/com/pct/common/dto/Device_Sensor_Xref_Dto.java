package com.pct.common.dto;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.model.Device;
import com.pct.common.model.UserDateAudit;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Device_Sensor_Xref_Dto extends UserDateAudit{

	
	private String sensorUuid;
	private DeviceStatus status;
    private String productCode;
    private String macAddress;
    private Instant datetimeCreated;
    private Instant datetimeUpdated;
    private String displayName;
    

    
	public Device_Sensor_Xref_Dto() {
		super();
	}
	
	
	public Device_Sensor_Xref_Dto(String sensorUuid, DeviceStatus status, String productCode,
			String macAddress, Instant datetimeCreated, Instant datetimeUpdated, String displayName) {
		super();
		this.sensorUuid = sensorUuid;
		this.status = status;
		this.productCode = productCode;	
		this.macAddress = macAddress;
		this.datetimeCreated = datetimeCreated;
		this.datetimeUpdated = datetimeUpdated;
		this.displayName = displayName;
	}
	public String getSensorUuid() {
		return sensorUuid;
	}
	public void setSensorUuid(String sensorUuid) {
		this.sensorUuid = sensorUuid;
	}
	
	public DeviceStatus getStatus() {
		return status;
	}


	public void setStatus(DeviceStatus status) {
		this.status = status;
	}


	public String getProductCode() {
		return productCode;
	}
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	public Instant getDatetimeCreated() {
		return datetimeCreated;
	}
	public void setDatetimeCreated(Instant datetimeCreated) {
		this.datetimeCreated = datetimeCreated;
	}
	public Instant getDatetimeUpdated() {
		return datetimeUpdated;
	}
	public void setDatetimeUpdated(Instant datetimeUpdated) {
		this.datetimeUpdated = datetimeUpdated;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	


	
}
