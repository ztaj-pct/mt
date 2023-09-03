package com.pct.device.service.device;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.util.JSONPObject;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class DeviceReport implements Serializable {

private static final long serialVersionUID = 1L;
	
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

private String  DEVICE_ID;

	
	private String BASEBAND_SW_VERSION;
	private String APP_SW_VERSION;
	private String EXTENDER_VERSION;
	private String SERVER_INFO_STR;
	// CLD-898
	private String BLE_VERSION;
	public String config1CRC;
	public String config1CIV;

	public String config2CRC;
	public String config2CIV;

	public String config3CRC;
	public String config3CIV;

	public String config4CRC;
	public String config4CIV;

	public String config5CIV;
	public String config5CRC;

	private String configurationDesc;
	private String customer;

	private String isDeviceInstalledForCampaign;
	private String deviceInstalledForCampaignDT;
	private String isDeviceUpgradeEligibleForCampaign;
	private String deviceUpgradeEligibleForCampaignDT;

	private String device_type;

	// for CLD-958
	private String liteSentryStatus;
	private String liteSentryHw;
	private String liteSentryApp;
	private String liteSentryBoot;
	private String maxbotixStatus;
	private String maxbotixFirmware;
	private String maxbotixhardware;

	private String steStatus;
	private String steMCU;
	private String steApp;

	private String riotStatus;
	private String riotFirmware;
	private String riothardware;
	private String device_id;

    private String sequence_number;
    
	private Instant created_date;
	
	private Instant created_epoch;
	
	private Instant updated_date;
	
	private String uuid;

	private String  raw_report;
	
	public Integer event_id;
	
	private String  parsed_report;
	
	public String device_ip;
	
	public Integer device_port;

	// Additional Variable
	   

}
