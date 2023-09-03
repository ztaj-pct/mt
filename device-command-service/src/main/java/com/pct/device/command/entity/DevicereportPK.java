package com.pct.device.command.entity;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the devicereport database table.
 * 
 */
@Embeddable
public class DevicereportPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="REPORT_ID")
	private long reportId;

	@Column(name="DEVICE_ID")
	private String deviceId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="TIMESTAMP_RECEIVED")
	private java.util.Date timestampReceived;

	public DevicereportPK() {
	}
	public long getReportId() {
		return this.reportId;
	}
	public void setReportId(long reportId) {
		this.reportId = reportId;
	}
	public String getDeviceId() {
		return this.deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public java.util.Date getTimestampReceived() {
		return this.timestampReceived;
	}
	public void setTimestampReceived(java.util.Date timestampReceived) {
		this.timestampReceived = timestampReceived;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof DevicereportPK)) {
			return false;
		}
		DevicereportPK castOther = (DevicereportPK)other;
		return 
			(this.reportId == castOther.reportId)
			&& this.deviceId.equals(castOther.deviceId)
			&& this.timestampReceived.equals(castOther.timestampReceived);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + ((int) (this.reportId ^ (this.reportId >>> 32)));
		hash = hash * prime + this.deviceId.hashCode();
		hash = hash * prime + this.timestampReceived.hashCode();
		
		return hash;
	}
}