package com.pct.common.dto;

import java.io.Serializable;
import java.sql.Date;
import java.time.Instant;

import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;

import lombok.Data;

@Data
public class MsDeviceRestResponse implements Serializable {

	
	 
		private Long id;

	 
		public MsDeviceRestResponse(String imei, Long organisationId, String organisationName, String installedStatusFlag) {
			super();
			this.imei = imei;
			this.organisationId = organisationId;
			this.organisationName = organisationName;
			this.installedStatusFlag = installedStatusFlag ;
		}
		
		private Instant createdAt;

	 	public MsDeviceRestResponse() {
  		}

		private Instant updatedAt;

	 // 	private Cellular cellular;

	 	private String imei;

	 	private String productCode;

	 	private String productName;

	 
		private IOTType iotType;

	 	private String deviceType;

	 
		private DeviceStatus status;

	 
		private OrganisationsDTO organisationsDto;

	 	private String macAddress;

	 	private String uuid;

	 
		private UserResponseDto createdBy;

	 
		private UserResponseDto updatedBy;

	 
		private String son;

	 	private String config1;

	 	private String other2Version;

	 	private String other1Version;

	 	private String bleVersion;

		private String binVersion;

		private String mcuVersion;

		private String appVersion;

		private String config2;

		private String config3;

		private String config4;

		private String qaStatus;

		private String usageStatus;

		private String epicorOrderNumber;

		private String quantityShipped;

		//private List<SensorDetail> sensorDetail;

		private Instant qaDate;

		private Date latestReport;


		//private List<DeviceForwarding> deviceForwarding;

		private String ownerLevel2;

		private String installedDate;

		private Date installedDateTimestamp;

		//private Asset_Device_xref assetDeviceXref;

		private String purchasedBy;


		private DeviceDetailsDto deviceDetailsDto;

		private String retriveStatus;
		private Long organisationId;
 		private String organisationName;
 		private String installedStatusFlag;

}
