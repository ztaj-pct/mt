package com.pct.common.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "latest_device_report_count", catalog = "pct_device")
@Getter
@Setter
public class LatestDeviceReportCount implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "device_id", columnDefinition = "VARCHAR(50)", nullable = true)
	private String deviceId;

	@Column(name = "ms2date_time")
	private Date ms2dateTime;

	@Column(name = "ms2last_record_id")
	private Long ms2lastRecordId;
	
	@Column(name = "ms2recordcount")
	private int ms2recordcount;
	
	@Column(name = "ms1recordcount")
	private int ms1recordcount;
	
	@Column(name = "ms1date_time")
	private Date ms1dateTime;
	
	@Column(name = "ms1last_record_id")
	private Long ms1lastRecordId;
	
	@Column(name = "can")
	private String can;
}
