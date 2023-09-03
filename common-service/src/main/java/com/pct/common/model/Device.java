package com.pct.common.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;
import com.pct.common.serde.InstantDeserializer;
import com.pct.common.serde.InstantSerializer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "device", catalog = "pct_device")
@Getter
@Setter
@Audited
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Device extends DateAudit implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "created_at")
	private Instant createdAt;

	@Column(name = "updated_at")
	private Instant updatedAt;

	@NotAudited
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "cellular_uuid", referencedColumnName = "uuid")
	private Cellular cellular;

	@Column(name = "imei", columnDefinition = "VARCHAR(50)", nullable = true)
	private String imei;

	@Column(name = "product_code", columnDefinition = "VARCHAR(255)", nullable = true)
	private String productCode;

	@Column(name = "product_name", columnDefinition = "VARCHAR(255)", nullable = false)
	private String productName;

	@NotAudited
	@Column(name = "iot_type")
	@Enumerated(value = EnumType.STRING)
	private IOTType iotType;

	@Column(name = "device_type")
	private String deviceType;

	
	@NotAudited
	@Column(name = "status")
	@Enumerated(value = EnumType.STRING)
	private DeviceStatus status;

//	@ManyToOne(cascade = { CascadeType.MERGE }, fetch=FetchType.EAGER)
	
	@ManyToOne
	@JoinColumn(name = "can", referencedColumnName = "account_number")
	private Organisation organisation;

	@Column(name = "mac_address")
	private String macAddress;

	@Column(name = "uuid")
	private String uuid;

	@JsonIgnoreProperties("role")
	@ManyToOne
	@JoinColumn(name = "created_by", referencedColumnName = "uuid")
	private User createdBy;

	@JsonIgnoreProperties("role")
	@ManyToOne
	@JoinColumn(name = "updated_by", referencedColumnName = "uuid")
	private User updatedBy;
	
//	@OneToOne
//    @JoinColumn(name = "asset_uuid", referencedColumnName = "uuid")
//    private Asset asset;

	@Column(name = "son", columnDefinition = "VARCHAR(255)")
	private String son;

	@Column(name = "qa_status", columnDefinition = "VARCHAR(255)")
	private String qaStatus;

	@Column(name = "usage_status", columnDefinition = "VARCHAR(255)")
	private String usageStatus;

	@Column(name = "epicor_order_number", columnDefinition = "VARCHAR(255)")
	private String epicorOrderNumber;

	@Column(name = "quantity_shipped", columnDefinition = "VARCHAR(255)")
	private String quantityShipped;

	@JsonIgnoreProperties(value = {"sensorUUID"}, allowSetters = true)
	@NotAudited
	@OneToMany(mappedBy = "sensorUUID", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<SensorDetail> sensorDetail;

	@Column(name = "qa_date")
	private Instant qaDate;

	@NotAudited
	@OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<DeviceForwarding> deviceForwarding;

	@Column(name = "owner_level_2")
	private String ownerLevel2;

	@NotAudited
	@OneToOne
	@JoinColumn(name = "asset_device", referencedColumnName = "id")
	@JsonIgnoreProperties("device")
	private Asset_Device_xref assetDeviceXref;

	@NotAudited
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "device_details_id", referencedColumnName = "imei")
	private DeviceDetails deviceDetails;
	
	@NotAudited
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "device_maintenance_details_id", referencedColumnName = "imei")
	private DeviceMaintenanceReportDetail deviceMaintenanceDetails;

	@Column(name = "retrive_status")
	private String retriveStatus;
	
	@NotAudited
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "device_signature_id", referencedColumnName = "id")
	private DeviceSignature deviceSignature;
	
	@JsonSerialize(using = InstantSerializer.class)
	@JsonDeserialize(using = InstantDeserializer.class)
	@Column(name = "time_of_last_download")
	private Instant timeOfLastDownload;

	@Column(name = "last_perform_action", columnDefinition = "VARCHAR(255) DEFAULT 'Updated'")
	private String lastPerformAction;

	@Column(name = "is_deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
	private Boolean isDeleted = false;
	
	@Column(name = "salesforce_order_id", columnDefinition = "VARCHAR(255)")
	private String  SalesforceOrderId;
	@ManyToOne
	@JoinColumn(name = "purchase_by", referencedColumnName = "account_number")
	private Organisation purchaseBy;
	
	@ManyToOne
	@JoinColumn(name = "installed_by", referencedColumnName = "account_number")
	private Organisation installedBy;
	
	@Column(name = "is_active")
	private Boolean isActive;
	
	@Column(name = "installed_status_flag")
	private String installedStatusFlag;
	  
	@JsonSerialize(using = InstantSerializer.class)
	@JsonDeserialize(using = InstantDeserializer.class)
	@Column(name = "installation_date")
	private Instant installationDate;
	
	@Column(name = "config_name",columnDefinition = "VARCHAR(255)")
	private String configName;

	@Column(name = "old_mac_address")
	private String oldMacAddress;

	@Column(name = "comment")
	private String comment;
}