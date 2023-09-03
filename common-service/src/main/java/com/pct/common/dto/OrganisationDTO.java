package com.pct.common.dto;

import java.time.Instant;
import java.util.Set;

import com.pct.common.constant.OrganisationRole;

//import com.pct.common.model.OrganisationRole;

public class OrganisationDTO {

	private Long id;

	private String organisationName;

	private String shortName;

	private Instant createdAt;

	private Instant updatedAt;

	private Boolean isActive;

	private String accountNumber;

	private String uuid;

	private Boolean isAssetListRequired;

	private String createdByUuid;

	private String updatedByUuid;

	private String previousRecordId;

	private String epicorAccountNumber;

	private Set<OrganisationRole> organisationRole;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOrganisationName() {
		return organisationName;
	}

	public void setOrganisationName(String organisationName) {
		this.organisationName = organisationName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
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

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Boolean getIsAssetListRequired() {
		return isAssetListRequired;
	}

	public void setIsAssetListRequired(Boolean isAssetListRequired) {
		this.isAssetListRequired = isAssetListRequired;
	}

	public String getCreatedByUuid() {
		return createdByUuid;
	}

	public void setCreatedByUuid(String createdByUuid) {
		this.createdByUuid = createdByUuid;
	}

	public String getUpdatedByUuid() {
		return updatedByUuid;
	}

	public void setUpdatedByUuid(String updatedByUuid) {
		this.updatedByUuid = updatedByUuid;
	}

	public String getPreviousRecordId() {
		return previousRecordId;
	}

	public void setPreviousRecordId(String previousRecordId) {
		this.previousRecordId = previousRecordId;
	}

	public String getEpicorAccountNumber() {
		return epicorAccountNumber;
	}

	public void setEpicorAccountNumber(String epicorAccountNumber) {
		this.epicorAccountNumber = epicorAccountNumber;
	}

	public Set<OrganisationRole> getOrganisationRole() {
		return organisationRole;
	}

	public void setOrganisationRole(Set<OrganisationRole> organisationRole) {
		this.organisationRole = organisationRole;
	}

}
