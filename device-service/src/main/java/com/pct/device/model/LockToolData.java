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
@Table(name = "lock_tool_data", catalog = "pct_device")
@Getter
@Setter
public class LockToolData extends UserDateAudit implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "uuid", columnDefinition = "VARCHAR(255)")
	private String uuid;
	
	@Column(name = "imei", columnDefinition = "VARCHAR(255)")
	private String imei;
	
	@Column(name = "event_timestamp", columnDefinition = "VARCHAR(255)")
	private String eventTimestamp;
	
	@Column(name = "event_type", columnDefinition = "VARCHAR(255)")
	private String eventType;
	  

	@ManyToOne(cascade = {CascadeType.PERSIST})
	@JsonIgnore
	@JoinColumn(name = "lock_tool_id", columnDefinition = "bigint", referencedColumnName = "id")
	private LockTool lockTool;


	@Override
	public String toString() {
		return "LockToolData [id=" + id + ", uuid=" + uuid + ", imei=" + imei + ", eventTimestamp=" + eventTimestamp
				+ ", eventType=" + eventType  + "]";
	}

	

	
	
	
	 
}