package com.pct.device.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.pct.common.model.UserDateAudit;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bat_test", catalog = "pct_device")
@Getter
@Setter
public class BatTest extends UserDateAudit implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "uuid", columnDefinition = "VARCHAR(255)")
	private String uuid;
	
	@Column(name = "tpms_mac", columnDefinition = "VARCHAR(255)")
	private String tpmsMac;
	
	@Column(name = "voltage", columnDefinition = "VARCHAR(255)")
	private String voltage;

	@Override
	public String toString() {
		return "BatTest [id=" + id + ", uuid=" + uuid + ", tpmsMac=" + tpmsMac + ", voltage=" + voltage + "]";
	}
	
	
}