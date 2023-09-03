package com.pct.common.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sensor_detail", catalog = "pct_device")
@Getter
@Setter
public class SensorDetail extends UserDateAudit implements Serializable{
	
	private static final long serialVersionUID = 1L;

	public static final String CREATED_BY = "created_by";
	
	public static final String LAST_MODIFIED_BY = "updated_by";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "position")
	private String position;

	@Column(name = "type")
	private String type;
	
	@Column(name = "sensor_id")
	private String sensorId;

	@Column(name = "old_sensor_id")
	private String oldSensorId;

	@Column(name = "uuid")
	private String uuid;
	
	@JsonIgnoreProperties(value = {"sensorDetail"}, allowSetters = true)
	@ManyToOne
	@JoinColumn(name = "sensor_uuid", referencedColumnName = "uuid", nullable = false)
	private Device sensorUUID;
	
	@JsonIgnoreProperties(value = {"sensorDetail"}, allowSetters = true)
	@NotAudited
	@OneToMany(mappedBy = "sensorDetail", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<InstallLog> InstallLog;
	
	@Column(name = "sensor_pressure")
	private String sensorPressure;

	@Column(name = "sensor_temperature")
	private String sensorTemperature;
	
	@Column(name = "device_id")
	private String deviceId;
	
	@Column(name = "product_code")
	private String productCode;
	
	@Column(name = "vendor")
	private String vendor;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "at_command_uuid")
	private String atCommandUuid;
	
	@Column(name = "at_command_status")
	private String atCommandStatus;
	
	@Column(name = "new_senso_id")
	private String newSensorId;
	
		@CreatedBy
	@JsonIgnoreProperties("role")
	@ManyToOne
	@JoinColumn(name = CREATED_BY, referencedColumnName = "uuid")
	private User createdBy;
	
	@LastModifiedBy
	@JsonIgnoreProperties("role")
	@ManyToOne
	@JoinColumn(name = LAST_MODIFIED_BY, referencedColumnName = "uuid")
	private User updatedBy;
}
