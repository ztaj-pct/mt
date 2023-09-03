package com.pct.common.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "device_signature", catalog = "pct_device")
@Getter
@Setter
public class DeviceSignature  implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "imei", nullable = true)
	private String imei;
	
	@Column(name = "imei_hashed")
	private String imeiHashed;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time")
	private Date createdTime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "revoked_time")
	private Date revokedTime;
	
	@Column(name = "is_report_signing_enabled")
	private Boolean reportSigningEnabled;
	
	@Column(name = "is_response_verification_enabled")
	private Boolean responseVerificationEnabled;
	
	@OneToOne(mappedBy = "deviceSignature")
	private Device device;

}
