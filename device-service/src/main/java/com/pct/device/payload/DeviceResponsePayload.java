package com.pct.device.payload;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.Column;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;
import com.pct.common.model.Asset;
import com.pct.common.model.Asset_Device_xref;
import com.pct.common.model.Device;
import com.pct.common.model.Organisation;
import com.pct.device.util.AppUtility;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceResponsePayload {

	private Organisation organisation;
	private Asset_Device_xref asset_Device_xref;
//	private Asset asset;
	private Long id;
	private String imei;
	private String deviceId;
	private String productCode;
	private String productName;
	private String model;
	private IOTType type;
	private String quantityShipped;
	private String appVersion;
	private String mcuVersion;
	private String binVersion;
	private String bleVersion;
	private String other1Version;
	private String other2Version;
	private String config1;
	private String config2;
	private String config3;
	private String config4;
	private String epicorOrderNumber;
	private String can;
	private String organisationName;
	private String son;
	private String order;
	private String macAddress;
    private String createdBy;
    private String customer;
    private String updatedby;
    private String uuid;
    private String qaStatus;
    private Instant qaDate;
    private String qaDates;
    private String usageStatus;
    private DeviceStatus status;
	private String lastReportDate;
	private Date lastReportDateTime;
    private CellularDetailPayload cellularPayload;
    @JsonIgnore
    private List<DeviceForwardingResponse> deviceForwardingPayload;
    private String ownerLevel2;
    private String sim;
	private String carrierId;
	private String serviceNetwork;
	private String serviceCountry;
	private String countryCode;
	private String phone;
	private String type1;
	private String url1;
	private String type2;
	private String url2;
	private String type3;
	private String url3;
	private String type4;
	private String url4;
	private String type5;
	private String url5;
	private String config1Name;
	private String config1CRC;
	private String config2Name;
	private String config2CRC;
	private String config3Name;
	private String config3CRC;
	private String config4Name;
	private String config4CRC;
	private String devuserCfgName;
	private String devuserCfgValue;
	private String assetName;
	private String assetType;
	private String installedDate;
	private Date installedDateTimestamp;
	private String vin;
	private String manufacturer;
	private int eventId;
	private String eventType;
	private Float battery;
	private Float lat;
	private Float longitude;
	private String purchasedBy;
	private Date createdTime;
	private String imeiHashed;
	private Date revokedTime;
	private String secStatus;
	private Date secDate;
	private String rule1;
	private String rule1Source;
	private String rule2;
	private String rule2Source;
	private String rule3;
	private String rule3Source;
	private String rule4;
	private String rule4Source;
	private String rule5;
	private String rule5Source;
	private String purchaseBy;
	private String installedBy;
	private String forwardingGroup;
	private String purchaseByName;
	private String  hardwareIdVersion;
	private String hardwareversionRevision;
	private String installedStatusFlag;
	private Instant installationDate;
	private String configName;
	private String deviceType;
}
