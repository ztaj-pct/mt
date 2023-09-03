package com.pct.common.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "device_qa", catalog = "pct_device")
public class DeviceQa  extends DateAudit implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "qa_status", columnDefinition = "VARCHAR(255)")
	private String qaStatus;
	
	@Column(name = "uuid")
	private String uuid;
	
	@Column(name = "qa_result", columnDefinition = "VARCHAR(255)")
	private String qaResult;
	
	@Column(name = "qa_date")
	private Timestamp qaDate;
	
	@OneToOne(cascade = {CascadeType.ALL})
	@JoinColumn(name = "device_id", referencedColumnName = "imei")
	private Device deviceId;



   
	
}
