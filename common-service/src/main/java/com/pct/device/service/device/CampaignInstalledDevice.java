package com.pct.device.service.device;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CampaignInstalledDevice implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private BigInteger RECORD_ID;
	
	private String DEVICE_ID;
	
	private Boolean QA_STATUS;
	
	private Timestamp QA_TIMESTAMP;
	
	private Boolean LATEST_MAINTENANCE_QA;
	
	private Timestamp LATEST_MAINTENANCE_QA_TIMESTAMP;
	
	private Boolean IS_POWER_UP_AFTER_QA;
	
	private Timestamp FIRST_POWER_UP_TIMESTAMP_AFTER_QA;
	
	private BigInteger POWER_UP_REPORT_COUNTER;
	
	private Boolean IS_INSTALLED_FOR_CAMPAIGN;
	
	private Timestamp IS_INSTALLED_FOR_CAMPAIGN_TIMESTAMP;
	
	private Boolean IS_EXISTING_DB_DEVICE;
	
	private Timestamp LAST_UPDATED_AT;

}
