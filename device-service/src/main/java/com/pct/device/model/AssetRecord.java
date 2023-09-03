package com.pct.device.model;

import org.hibernate.annotations.Immutable;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Immutable
@Table(name = "asset_record")
public class AssetRecord {
	
	@Column(name = "created_first_name")
	String createdFirstName;
	
	@Column(name = "created_last_name")
	String createdLastName;
	
	@Column(name = "organisation_name")
	String organisationName;
	
	@Column(name = "updated_first_name")
	String updatedFirstName;
	
	@Column(name = "updated_last_name")
	String updatedLastName;
	
	@Column(name = "count")
	Integer count;
	
	@Column(name = "created_at")
	Instant createdAt;
	
	@Column(name = "updated_at")
	Instant updatedAt;
	
	@Id
	@Column(name = "organisation_id")
	Long organisationId;
	
	@Column(name = "forwarding_group")
	String forwardingGroup;
	
	@Column(name = "parent_group")
	String parentGroup;

	public String getCreatedFirstName() {
		return createdFirstName;
	}

	public void setCreatedFirstName(String createdFirstName) {
		this.createdFirstName = createdFirstName;
	}

	public String getCreatedLastName() {
		return createdLastName;
	}

	public void setCreatedLastName(String createdLastName) {
		this.createdLastName = createdLastName;
	}


	public String getUpdatedFirstName() {
		return updatedFirstName;
	}

	public void setUpdatedFirstName(String updatedFirstName) {
		this.updatedFirstName = updatedFirstName;
	}

	public String getUpdatedLastName() {
		return updatedLastName;
	}

	public void setUpdatedLastName(String updatedLastName) {
		this.updatedLastName = updatedLastName;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
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

	public String getOrganisationName() {
		return organisationName;
	}

	public void setOrganisationName(String organisationName) {
		this.organisationName = organisationName;
	}

	public Long getOrganisationId() {
		return organisationId;
	}

	public void setOrganisationId(Long organisationId) {
		this.organisationId = organisationId;
	}

	public String getForwardingGroup() {
		return forwardingGroup;
	}

	public void setForwardingGroup(String forwardingGroup) {
		this.forwardingGroup = forwardingGroup;
	}

	public String getParentGroup() {
		return parentGroup;
	}

	public void setParentGroup(String parentGroup) {
		this.parentGroup = parentGroup;
	}
	
	

}
