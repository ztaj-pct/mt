package com.pct.device.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pct.common.model.UserDateAudit;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tire_tool_sensor", catalog = "pct_device")
@Getter
@Setter
public class TireToolSensor extends UserDateAudit implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "uuid", columnDefinition = "VARCHAR(255)")
	private String uuid;
	
	@Column(name = "sensor_location", columnDefinition = "VARCHAR(255)")
	private String sensorLocation;
	
	@Column(name = "sensor_id", columnDefinition = "VARCHAR(255)")
	private String sensorId;
	
	@Column(name = "sensor_type_description", columnDefinition = "VARCHAR(255)")
	private String sensorTypeDescription;
	
	@ManyToOne(cascade = {CascadeType.PERSIST})
	@JsonIgnore
	@JoinColumn(name = "tire_tool_id", columnDefinition = "bigint", referencedColumnName = "id")
	private TireTool tireTool;

	@Override
	public String toString() {
		return "TireToolSensor [id=" + id + ", uuid=" + uuid + ", sensorLocation=" + sensorLocation + ", sensorId="
				+ sensorId + ", sensorTypeDescription=" + sensorTypeDescription + "]";
	}
	
	

}