package com.pct.common.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "cellular", catalog = "pct_device")
public class Cellular implements Serializable{
	
	 private static final long serialVersionUID = 1L; 
	 
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@Column(name="cellular")
	private String cellular;
	
	@Column(name="uuid")
	private String uuid;
	
	@Column(name="imei")
	private String imei;
	
	@Column(name="iccid")
	private String iccid;

	@Column(name="imsi")
    private String imsi;
	
	@Column(name="carrier_id")
	private String carrierId;
	
	@Column(name="service_network")
	private String serviceNetwork;
	
	@Column(name="service_country")
	private String serviceCountry;
	
	
	@Column(name="country_code")
	private String countryCode;
	
	@Column(name="phone")
	private String phone;
	
//	@Column(name="sim_number")
//	private String simNumber;
}
