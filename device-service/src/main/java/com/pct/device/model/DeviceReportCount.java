package com.pct.device.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "device_report_count_detail")
public class DeviceReportCount {

	@Id
	@Column(name = "ms2_organisation_name")
	String ms2OrganisationName;
	
	@Column(name = "ms1recordcount")
	int ms1recordcount;
	
	@Column(name = "ms2Count")
	int ms2Count;
	
	public String getMs2OrganisationName() {
		return ms2OrganisationName;
	}

	public void setMs2OrganisationName(String ms2OrganisationName) {
		this.ms2OrganisationName = ms2OrganisationName;
	}

	public int getMs1recordcount() {
		return ms1recordcount;
	}

	public void setMs1recordcount(int ms1recordcount) {
		this.ms1recordcount = ms1recordcount;
	}

	public int getMs2Count() {
		return ms2Count;
	}

	public void setMs2Count(int ms2Count) {
		this.ms2Count = ms2Count;
	}
	
}
