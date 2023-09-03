package com.pct.common.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.NoArgsConstructor;

@Entity
@Table(name = "sensor_history_for_installation", catalog = "pct_installer_ms")
@NoArgsConstructor
public class SensorHistoryForInstallation extends DateAudit implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "created_by_user_uuid")
	private String userUuid;

	@Column(name = "updated_by_user_uuid")
	private String updatedUserUuid;

	@Column(name = "created_by_user_name")
	private String userName;

	@Column(name = "updated_by_user_name")
	private String updatedUserName;

	@Column(name = "asset_uuid")
	private String assetUuid;

	@Column(name = "gateway_uuid")
	private String gatewayUuid;

	@Column(name = "install_code")
	private String installCode;

	@Column(name = "sensor_uuid")
	private String sensorUuid;

	@Column(name = "product_name")
	private String productName;

	@Column(name = "product_code")
	private String productCode;

	@Column(name = "action")
	private String action;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

	public String getUpdatedUserUuid() {
		return updatedUserUuid;
	}

	public void setUpdatedUserUuid(String updatedUserUuid) {
		this.updatedUserUuid = updatedUserUuid;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUpdatedUserName() {
		return updatedUserName;
	}

	public void setUpdatedUserName(String updatedUserName) {
		this.updatedUserName = updatedUserName;
	}

	public String getAssetUuid() {
		return assetUuid;
	}

	public void setAssetUuid(String assetUuid) {
		this.assetUuid = assetUuid;
	}

	public String getGatewayUuid() {
		return gatewayUuid;
	}

	public void setGatewayUuid(String gatewayUuid) {
		this.gatewayUuid = gatewayUuid;
	}

	public String getInstallCode() {
		return installCode;
	}

	public void setInstallCode(String installCode) {
		this.installCode = installCode;
	}

	public String getSensorUuid() {
		return sensorUuid;
	}

	public void setSensorUuid(String sensorUuid) {
		this.sensorUuid = sensorUuid;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

}
