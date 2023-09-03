package com.pct.organisation.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CreateOrganisationPayload {

	private String OrganisationName;
	private String extId;
	private Boolean isAssetListRequired;
	private Boolean status;
	private Boolean isApprovalReqForDeviceUpdate;
	private Boolean isTestReqBeforeDeviceUpdate;
	private Boolean isMonthlyReleaseNotesReq;
	private Boolean isDigitalSignReqForFirmware;
	private Boolean isAutoResetInstallation;
	private Long noOfDevice;
	
	
	public String getOrganisationName() {
		return OrganisationName;
	}
	public void setOrganisationName(String organisationName) {
		OrganisationName = organisationName;
	}
	public String getExtId() {
		return extId;
	}
	public void setExtId(String extId) {
		this.extId = extId;
	}
	public Boolean getIsAssetListRequired() {
		return isAssetListRequired;
	}
	public void setIsAssetListRequired(Boolean isAssetListRequired) {
		this.isAssetListRequired = isAssetListRequired;
	}
	public Boolean getStatus() {
		return status;
	}
	public void setStatus(Boolean status) {
		this.status = status;
	}
	public Boolean getIsApprovalReqForDeviceUpdate() {
		return isApprovalReqForDeviceUpdate;
	}
	public void setIsApprovalReqForDeviceUpdate(Boolean isApprovalReqForDeviceUpdate) {
		this.isApprovalReqForDeviceUpdate = isApprovalReqForDeviceUpdate;
	}
	public Boolean getIsTestReqBeforeDeviceUpdate() {
		return isTestReqBeforeDeviceUpdate;
	}
	public void setIsTestReqBeforeDeviceUpdate(Boolean isTestReqBeforeDeviceUpdate) {
		this.isTestReqBeforeDeviceUpdate = isTestReqBeforeDeviceUpdate;
	}
	public Boolean getIsMonthlyReleaseNotesReq() {
		return isMonthlyReleaseNotesReq;
	}
	public void setIsMonthlyReleaseNotesReq(Boolean isMonthlyReleaseNotesReq) {
		this.isMonthlyReleaseNotesReq = isMonthlyReleaseNotesReq;
	}
	public Boolean getIsDigitalSignReqForFirmware() {
		return isDigitalSignReqForFirmware;
	}
	public void setIsDigitalSignReqForFirmware(Boolean isDigitalSignReqForFirmware) {
		this.isDigitalSignReqForFirmware = isDigitalSignReqForFirmware;
	}
	public Boolean getIsAutoResetInstallation() {
		return isAutoResetInstallation;
	}
	public void setIsAutoResetInstallation(Boolean isAutoResetInstallation) {
		this.isAutoResetInstallation = isAutoResetInstallation;
	}
	public Long getNoOfDevice() {
		return noOfDevice;
	}
	public void setNoOfDevice(Long noOfDevice) {
		this.noOfDevice = noOfDevice;
	}
	
	
}
