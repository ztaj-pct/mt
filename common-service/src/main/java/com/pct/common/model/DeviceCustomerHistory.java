package com.pct.common.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "device_customer_history", catalog = "pct_device")
@Getter
@Setter
@Audited
public class DeviceCustomerHistory extends UserDateAudit{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "imei")
	private String imei;
	
	@Column(name = "account_number")
	private String accountNumber;
	
}
