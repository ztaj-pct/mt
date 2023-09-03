package com.pct.organisation.payload;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.constant.OrganisationRole;
import com.pct.common.payload.OrganisationIdPayload;


@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AddOrganisationPayload {
	
	@JsonProperty("id")
	private Long id;
	
	@JsonProperty("organisation_name")
	private String OrganisationName;

	@JsonProperty("short_name")
	private String shortName;

	@JsonProperty("status")
	private Boolean status;

//	@JsonProperty("type")
//	private OrganisationType type;

	@JsonProperty("account_number")
	private String accountNumber;

	@JsonProperty("record_id")
	private String recordId;

	@JsonProperty("epicor_id")
	private String epicorId;

	@JsonProperty("is_asset_list_required")
	private Boolean isAssetListRequired;

	@JsonProperty("is_approval_req_for_device_update")
	private Boolean isApprovalReqForDeviceUpdate;

	@JsonProperty("is_test_req_before_device_update")
	private Boolean isTestReqBeforeDeviceUpdate;

	@JsonProperty("is_monthly_release_notes_req")
	private Boolean isMonthlyReleaseNotesReq;

	@JsonProperty("is_digital_sign_req_for_firmware")
	private Boolean isDigitalSignReqForFirmware;
	
	@JsonProperty("is_auto_reset_installation")
	private Boolean isAutoResetInstallation;

	@JsonProperty("no_of_device")
	private int noOfDevice;
	
	@JsonProperty("access_id")
	private List<String> accessId;
	
	@JsonProperty("organisation_role")
    private Set<OrganisationRole> organisationRole;
	
	@JsonProperty("salesforce_account_number")
	private String salesforceAccountNumber;
	
	@JsonProperty("epicor_account_number")
	private String epicorAccountNumber;
	
	@JsonProperty("reseller_list")
	private List<OrganisationIdPayload> resellerList;
	
	@JsonProperty("installer_list")
	private List<OrganisationIdPayload> installerList;
	
	@JsonProperty("maintenance_list")
	private List<OrganisationIdPayload> maintenanceList;
	
	@JsonProperty("customer_forwarding_rules")
	private List<CustomerForwardingRulePayload> customerForwardingRules;
	
	@JsonProperty("customer_forwarding_groups")
	private List<CustomerForwardingGroupMapperPayload> customerForwardingGroups;

	public Set<OrganisationRole> getOrganisationRole() {
		return organisationRole;
	}

	public void setOrganisationRole(Set<OrganisationRole> organisationRole) {
		this.organisationRole = organisationRole;
	}

	public String getSalesforceAccountNumber() {
		return salesforceAccountNumber;
	}

	public void setSalesforceAccountNumber(String salesforceAccountNumber) {
		this.salesforceAccountNumber = salesforceAccountNumber;
	}

	public String getEpicorAccountNumber() {
		return epicorAccountNumber;
	}

	public void setEpicorAccountNumber(String epicorAccountNumber) {
		this.epicorAccountNumber = epicorAccountNumber;
	}

	public List<OrganisationIdPayload> getResellerList() {
		return resellerList;
	}

	public void setResellerList(List<OrganisationIdPayload> resellerList) {
		this.resellerList = resellerList;
	}

	public List<OrganisationIdPayload> getInstallerList() {
		return installerList;
	}

	public void setInstallerList(List<OrganisationIdPayload> installerList) {
		this.installerList = installerList;
	}

	public List<OrganisationIdPayload> getMaintenanceList() {
		return maintenanceList;
	}

	public void setMaintenanceList(List<OrganisationIdPayload> maintenanceList) {
		this.maintenanceList = maintenanceList;
	}


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getIsAutoResetInstallation() {
		return isAutoResetInstallation;
	}

	public void setIsAutoResetInstallation(Boolean isAutoResetInstallation) {
		this.isAutoResetInstallation = isAutoResetInstallation;
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public String getEpicorId() {
		return epicorId;
	}

	public void setEpicorId(String epicorId) {
		this.epicorId = epicorId;
	}

	public String getOrganisationName() {
		return OrganisationName;
	}

	public void setOrganisationName(String organisationName) {
		OrganisationName = organisationName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

//	public OrganisationType getType() {
//		return type;
//	}
//
//	public void setType(OrganisationType type) {
//		this.type = type;
//	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public Boolean getIsAssetListRequired() {
		return isAssetListRequired;
	}

	public void setIsAssetListRequired(Boolean isAssetListRequired) {
		this.isAssetListRequired = isAssetListRequired;
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

	public int getNoOfDevice() {
		return noOfDevice;
	}

	public void setNoOfDevice(int noOfDevice) {
		this.noOfDevice = noOfDevice;
	}

	public List<String> getAccessId() {
		return accessId;
	}

	public void setAccessId(List<String> accessId) {
		this.accessId = accessId;
	}

	public List<CustomerForwardingRulePayload> getCustomerForwardingRules() {
		return customerForwardingRules;
	}

	public void setCustomerForwardingRules(List<CustomerForwardingRulePayload> customerForwardingRules) {
		this.customerForwardingRules = customerForwardingRules;
	}

	public List<CustomerForwardingGroupMapperPayload> getCustomerForwardingGroups() {
		return customerForwardingGroups;
	}

	public void setCustomerForwardingGroups(List<CustomerForwardingGroupMapperPayload> customerForwardingGroups) {
		this.customerForwardingGroups = customerForwardingGroups;
	}
	
}
