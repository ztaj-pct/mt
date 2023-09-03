package com.pct.device.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;

import lombok.Data;

@Entity
@Table(name = "device_view")
@Immutable
@Data
public class DeviceView {
	
	@Id
	private Long id;
	
	@Column(name = "imei")
	private String imei;
	
	@Column(name = "uuid")
	private String uuid;
	
	@Column(name = "mac_address")
	private String macAddress;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "time_of_last_download")
	private Instant timeOfLastDownload;
	
//	@Column(name = "created_at")
//	private String gatewayUuid;
	
	@Column(name = "product_name")
	private String productName;
	
	@Column(name = "product_code")
	private String productCode;
	
	@Column(name = "created_on")
	private Instant createdOn;
	
	@Column(name = "updated_on")
	private Instant updatedOn;
	
	@Column(name = "can")
	private String can;
	
	@Column(name = "last_perform_action")
	private String lastPerformAction;
	
	@Column(name = "is_deleted")
	private Boolean isDeleted ;
	
	@Column(name = "iot_type")
	private String iotType;
}