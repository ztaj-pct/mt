package com.pct.device.model;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "device_summary")
public class DeviceSummary implements Serializable {

//	@Column(name = "created_first_name")
//	String createdFirstName;
//	
//	@Column(name = "created_last_name")
//	String createdLastName;
	
	@Column(name = "company_name")
	String companyName;
	
//	@Column(name = "updated_first_name")
//	String updatedFirstName;
//	
//	@Column(name = "updated_last_name")
//	String updatedLastName;
	
	@Column(name = "count")
	Integer count;
	
//	@Column(name = "created_at")
//	Instant createdAt;
//	
//	@Column(name = "updated_at")
//	Instant updatedAt;
	
	@Id
	@Column(name = "company_id")
	Long companyId;
	
//	public String getCreatedFirstName() {
//		return createdFirstName;
//	}
//
//	public void setCreatedFirstName(String createdFirstName) {
//		this.createdFirstName = createdFirstName;
//	}
//
//	public String getCreatedLastName() {
//		return createdLastName;
//	}
//
//	public void setCreatedLastName(String createdLastName) {
//		this.createdLastName = createdLastName;
//	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

//	public String getUpdatedFirstName() {
//		return updatedFirstName;
//	}
//
//	public void setUpdatedFirstName(String updatedFirstName) {
//		this.updatedFirstName = updatedFirstName;
//	}
//
//	public String getUpdatedLastName() {
//		return updatedLastName;
//	}
//
//	public void setUpdatedLastName(String updatedLastName) {
//		this.updatedLastName = updatedLastName;
//	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

//	public Instant getCreatedAt() {
//		return createdAt;
//	}
//
//	public void setCreatedAt(Instant createdAt) {
//		this.createdAt = createdAt;
//	}
//
//	public Instant getUpdatedAt() {
//		return updatedAt;
//	}
//
//	public void setUpdatedAt(Instant updatedAt) {
//		this.updatedAt = updatedAt;
//	}

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}
	
}
