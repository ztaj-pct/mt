package com.pct.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pct.common.constant.InstallHistoryStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Abhishek on 29/05/20
 */


public class InProgressInstall {

    @JsonProperty("gateway_uuid")
    private String gatewayUuid;
    @JsonProperty("asset_uuid")
    private String assetUuid;
    private String imei;
    @JsonProperty("mac_address")
    private String macAddress;
    private String vin;
    @JsonProperty("assigned_name")
    private String assignedName;
    @JsonProperty("install_code")
    private String installCode;
    private String status;
    
    
    
	public InProgressInstall() {
		super();
	}
	public InProgressInstall(String gatewayUuid, String assetUuid, String imei, String macAddress, String vin,
			String assignedName, String installCode, InstallHistoryStatus status) {
		super();
		this.gatewayUuid = gatewayUuid;
		this.assetUuid = assetUuid;
		this.imei = imei;
		this.macAddress = macAddress;
		this.vin = vin;
		this.assignedName = assignedName;
		this.installCode = installCode;
		if(status != null) {
			this.status = status.getValue();
		} else {
			this.status = null;
		}
		
	}
	public String getGatewayUuid() {
		return gatewayUuid;
	}
	public void setGatewayUuid(String gatewayUuid) {
		this.gatewayUuid = gatewayUuid;
	}
	public String getAssetUuid() {
		return assetUuid;
	}
	public void setAssetUuid(String assetUuid) {
		this.assetUuid = assetUuid;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	public String getVin() {
		return vin;
	}
	public void setVin(String vin) {
		this.vin = vin;
	}
	public String getAssignedName() {
		return assignedName;
	}
	public void setAssignedName(String assignedName) {
		this.assignedName = assignedName;
	}
	public String getInstallCode() {
		return installCode;
	}
	public void setInstallCode(String installCode) {
		this.installCode = installCode;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
    
    
}
