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
@Table(name = "tire_tool", catalog = "pct_device")
@Getter
@Setter
public class TireTool extends UserDateAudit implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "uuid", columnDefinition = "VARCHAR(255)")
	private String uuid;
	
	@Column(name = "receiver_type", columnDefinition = "VARCHAR(255)")
	private String receiverType;
	
	@Column(name = "receiver_type_description", columnDefinition = "VARCHAR(255)")
	private String receiverTypeDescription;
	
	@Column(name = "trailer_type", columnDefinition = "VARCHAR(255)")
	private String trailerType;
	
	@Column(name = "trailer_type_description", columnDefinition = "VARCHAR(255)")
	private String trailerTypeDescription;

	@Column(name = "receiver_serial_number", columnDefinition = "VARCHAR(255)")
	private String receiverSerialNumber;
	
	@Column(name = "scan_sheet_version", columnDefinition = "VARCHAR(255)")
	private String scanSheetVersion;
	
	@OneToMany(mappedBy = "tireTool", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<TireToolSensor> tireToolSensors;

	@Override
	public String toString() {
		return "TireTool [id=" + id + ", uuid=" + uuid + ", receiverType=" + receiverType + ", receiverTypeDescription="
				+ receiverTypeDescription + ", trailerType=" + trailerType + ", trailerTypeDescription="
				+ trailerTypeDescription + ", receiverSerialNumber=" + receiverSerialNumber + ", scanSheetVersion="
				+ scanSheetVersion + ", tireToolSensors=" + tireToolSensors + "]";
	}
	
	

}