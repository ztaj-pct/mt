package com.pct.device.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.pct.common.model.UserDateAudit;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "production_tool", catalog = "pct_device")
@Getter
@Setter
public class ProductionTool extends UserDateAudit implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "uuid", columnDefinition = "VARCHAR(255)")
	private String uuid;
    
	@Column(name = "tool_name", columnDefinition = "VARCHAR(255)")
	private String toolName;
	
	@Column(name = "tool_revision", columnDefinition = "VARCHAR(255)")
	private String toolRevision;
	
	@Column(name = "tool_ip_address", columnDefinition = "VARCHAR(255)")
	private String toolIpAddress;
	
	@Column(name = "tool_operator", columnDefinition = "VARCHAR(255)")
	private String toolOperator;
	
	@Column(name = "event_timestamp", columnDefinition = "VARCHAR(255)")
	private String eventTimestamp;
	
	@Column(name = "event_type", columnDefinition = "VARCHAR(255)")
	private String eventType;
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "bat_test_id", columnDefinition = "bigint" , referencedColumnName = "id")
	private BatTest batTest;
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "spare_tool_id", columnDefinition = "bigint", referencedColumnName = "id")
	private SpareTool spareTool;
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "tire_tool_id", columnDefinition = "bigint" , referencedColumnName = "id")
	private TireTool tireTool;
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "lock_tool_id", columnDefinition = "bigint" , referencedColumnName = "id")
	private LockTool lockTool;
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "free_tool_id", columnDefinition = "bigint" , referencedColumnName = "id")
	private FreeTool freeTool;

	@Override
	public String toString() {
		return "ProductionTool [id=" + id + ", uuid=" + uuid + ", toolName=" + toolName + ", toolRevision="
				+ toolRevision + ", toolIpAddress=" + toolIpAddress + ", toolOperator=" + toolOperator
				+ ", eventTimestamp=" + eventTimestamp + ", eventType=" + eventType + ", batTest=" + batTest
				+ ", spareTool=" + spareTool + ", tireTool=" + tireTool + ", lockTool=" + lockTool + ", freeTool="
				+ freeTool + "]";
	}

	
	
	

}