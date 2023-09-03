package com.pct.device.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.pct.common.model.UserDateAudit;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "spare_tool", catalog = "pct_device")
@Getter
@Setter
public class SpareTool extends UserDateAudit implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "uuid", columnDefinition = "VARCHAR(255)")
	private String uuid;
	
	@Column(name = "imei", columnDefinition = "VARCHAR(255)")
	private String imei;
	
	@OneToMany(mappedBy = "spareTool", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<SpareToolSensor> spareToolSensors;

	@Override
	public String toString() {
		return "SpareTool [id=" + id + ", uuid=" + uuid + ", imei=" + imei + ", spareToolSensors=" + spareToolSensors
				+ "]";
	}

	
	
	 
}