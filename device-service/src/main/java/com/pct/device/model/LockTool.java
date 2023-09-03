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
@Table(name = "lock_tool", catalog = "pct_device")
@Getter
@Setter
public class LockTool extends UserDateAudit implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "uuid", columnDefinition = "VARCHAR(255)")
	private String uuid;
	
	@OneToMany(mappedBy = "lockTool", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<LockToolData> lockList;

	@Override
	public String toString() {
		return "LockTool [id=" + id + ", uuid=" + uuid + ", lockList=" + lockList + "]";
	}

	
	
	 
}