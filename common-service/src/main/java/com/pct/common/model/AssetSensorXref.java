package com.pct.common.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "asset_sensor_xref", catalog = "pct_device")
@Getter
@Setter
@NoArgsConstructor
public class AssetSensorXref {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "asset_uuid", referencedColumnName = "uuid")
	private Asset asset;

	@ManyToOne
	@JoinColumn(name = "sensor_uuid", referencedColumnName = "uuid")
	private Device device;

	@Column(name = "is_active")
	private Boolean isActive;

	@Column(name = "date_created")
	private Instant dateCreated;

	@Column(name = "date_deleted")
	private Instant dateDeleted;
	
	@Column(name = "is_gateway_attached")
	private Boolean isGatewayAttached;

}
