package com.pct.common.dto;

import java.io.Serializable;
import java.util.Date;


import lombok.Data;


@Data
public class DeviceDetailsDto implements Serializable {

	private static final long serialVersionUID = 1L;

  	private Long id;

 	private String bleVersion;

 	private String binVersion;

 	private String mcuVersion;

 	private String appVersion;

 	private String config1Name;

 	private String config2Name;

 	private String config3Name;

 	private String config4Name;

 	private String config1CRC;

 	private String config2CRC;

 	private String config3CRC;

 	private String config4CRC;

 	private String devuserCfgName;

 	private String devuserCfgValue;

 	private Date latestReport;

 	private int eventId;

 	private String eventType;

 	private Float battery;

 	private Float lat;

 	private Float longitude;

 	private String imei;

 	//private DeviceD device;

}
