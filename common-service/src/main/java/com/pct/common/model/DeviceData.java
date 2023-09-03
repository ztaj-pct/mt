package com.pct.common.model;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;


@Data
public class DeviceData {

	

	public static final int NO_UPDATE_ID = 0;
	public static final int MIN_ID_LENGTH = 14;
	public static final int MAX_ID_LENGTH = 15;
	public static final String DEFAULT_OWNER = "engineering";

	public static final String USAGE_PRODUCTION = "production";
	public static final String USAGE_PILOT = "pilot";
	public static final String USAGE_DEMO = "demo";
	public static final String USAGE_ENGINEERING = "engineering";
	public static final String USAGE_RMA = "rma";
	public static final String USAGE_EOL = "eol";
	public static final String[] DEVICE_USAGES = new String[] { USAGE_ENGINEERING, USAGE_DEMO, USAGE_PILOT,
			USAGE_PRODUCTION, USAGE_RMA, USAGE_EOL };

	public static final String QA_STATUS_SUCCESS = "success";
	public static final String QA_STATUS_FAIL = "fail";
	public static final String[] QA_STATUSES = new String[] { QA_STATUS_SUCCESS, QA_STATUS_FAIL };

	public static final int DEVICE_CONFIG_NUM_DIGITS = 4;

	// fields to be saved in the database
	/////////////////////////////////////
	private int factoryBoxNum;
	private String deviceSerialNum = null;
	private String deviceModel = null;
	private String deviceID = null; // imei
	private String deviceSimNum = null;// ceelular
	private String devicePhoneNum;
	private String serviceCountry = null;
	private String serviceNetwork = null;
	private java.sql.Timestamp QATimestampPST = null;
	private String QAStatus = null;
	private String QAResult = null;// deviceQA
	private String deviceHWID = "0000";
	private String deviceHWIDVer = "00";

	private String swVersionBaseband = null;
	private String swVersionApplication = null;
	private String deviceConfigChanged;
	private int deviceConfig = 0;
	private String configurationDesc;
	private String deviceName = null;// product nane
	private String ownerLevel1 = null;
	private String ownerLevel2 = null;// company name
	private String ownerLevel3 = null;
	private String ownerLevel4 = null;
	private String salesforceOrderNumber = null;
	private String epicorOrderNumber = null;
	private String waterfallDevdef_cfgCRC_value;
	private String waterfallDevdef2_cfgCRC_value;
	private String waterfallDevdef3_cfgCRC_value;
	private String waterfallDevdef4_cfgCRC_value;
	private String waterfallDevusr_cfg_CRC_value;
	private String appVersionStr;
	private String osVersionStr;
	private String hwVersionStr;
	private String hwRevisionStr;
	private String extenderVersionStr;
	public String bleVersionStr;
	public java.sql.Timestamp lastReportedOn;
	private String comment = null;
	private int updateID;
	private String salesOrderID;// device epicor
	private int salesBoxNum;
	private int hwVersionIO = 0;
	private String vertical = null;
	private String deviceUsage = null;// devie
	private String productCode = null;// product code
	
	private String factoryPO = null;
	private String configName = null;
	
	@Override
	public String toString() {
		return "DeviceData [factoryBoxNum=" + factoryBoxNum + ", deviceSerialNum=" + deviceSerialNum + ", deviceModel="
				+ deviceModel + ", deviceID=" + deviceID + ", deviceSimNum=" + deviceSimNum + ", devicePhoneNum="
				+ devicePhoneNum + ", serviceCountry=" + serviceCountry + ", serviceNetwork=" + serviceNetwork
				+ ", QATimestampPST=" + QATimestampPST + ", QAStatus=" + QAStatus + ", QAResult=" + QAResult
				+ ", deviceHWID=" + deviceHWID + ", deviceHWIDVer=" + deviceHWIDVer + ", swVersionBaseband="
				+ swVersionBaseband + ", swVersionApplication=" + swVersionApplication + ", deviceConfigChanged="
				+ deviceConfigChanged + ", deviceConfig=" + deviceConfig + ", configurationDesc=" + configurationDesc
				+ ", deviceName=" + deviceName + ", ownerLevel1=" + ownerLevel1 + ", ownerLevel2=" + ownerLevel2
				+ ", ownerLevel3=" + ownerLevel3 + ", ownerLevel4=" + ownerLevel4 + ", salesforceOrderNumber="
				+ salesforceOrderNumber + ", epicorOrderNumber=" + epicorOrderNumber + ", waterfallDevdef_cfgCRC_value="
				+ waterfallDevdef_cfgCRC_value + ", waterfallDevdef2_cfgCRC_value=" + waterfallDevdef2_cfgCRC_value
				+ ", waterfallDevdef3_cfgCRC_value=" + waterfallDevdef3_cfgCRC_value
				+ ", waterfallDevdef4_cfgCRC_value=" + waterfallDevdef4_cfgCRC_value
				+ ", waterfallDevusr_cfg_CRC_value=" + waterfallDevusr_cfg_CRC_value + ", appVersionStr="
				+ appVersionStr + ", osVersionStr=" + osVersionStr + ", hwVersionStr=" + hwVersionStr
				+ ", hwRevisionStr=" + hwRevisionStr + ", extenderVersionStr=" + extenderVersionStr + ", bleVersionStr="
				+ bleVersionStr + ", lastReportedOn=" + lastReportedOn + ", comment=" + comment + ", updateID="
				+ updateID + ", salesOrderID=" + salesOrderID + ", salesBoxNum=" + salesBoxNum + ", hwVersionIO="
				+ hwVersionIO + ", vertical=" + vertical + ", deviceUsage=" + deviceUsage + ", productCode="
				+ productCode + ", factoryPO=" + factoryPO + ", configName=" + configName +"]";
	}
	
	
	
	
	

}
