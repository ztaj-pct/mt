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
import lombok.ToString;

@Entity
@Table(name = "spare_tool_sensor", catalog = "pct_device")
@Getter
@Setter
public class SpareToolSensor extends UserDateAudit implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "uuid", columnDefinition = "VARCHAR(255)")
	private String uuid;
	
	@Column(name = "sensor_mac", columnDefinition = "VARCHAR(255)")
	private String sensorMac;
	
	@Column(name = "sensor_type", columnDefinition = "VARCHAR(255)")
	private String sensorType;
	
	@Column(name = "sensor_type_description", columnDefinition = "VARCHAR(255)")
	private String sensorTypeDescription;
	
	@Column(name = "sensor_location", columnDefinition = "VARCHAR(255)")
	private String sensorLocation;

	@ManyToOne(cascade = {CascadeType.PERSIST})
	@JsonIgnore
	@JoinColumn(name = "spare_tool_id", columnDefinition = "bigint", referencedColumnName = "id")
	private SpareTool spareTool;

	@Override
	public String toString() {
		return "SpareToolSensor [id=" + id + ", uuid=" + uuid + ", sensorMac=" + sensorMac + ", sensorType="
				+ sensorType + ", sensorTypeDescription=" + sensorTypeDescription + ", sensorLocation=" + sensorLocation
				+ "]";
	}


	
	
	
	 
}