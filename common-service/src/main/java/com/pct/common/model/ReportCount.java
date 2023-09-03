package com.pct.common.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "report_count", catalog = "pct_device")
@Getter
@Setter
public class ReportCount  implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@Column(name = "device_id", columnDefinition = "VARCHAR(50)", nullable = true)
	private String deviceId;
	
	@Column(name = "count")
	private int count;
	
	@Column(name = "can")
	private String can;
	
	@ManyToOne
	@JoinColumn(name = "organisation_id", referencedColumnName = "uuid")
	private Organisation organisation;
}
