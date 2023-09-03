package com.pct.common.model;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "failed_device_qa", catalog = "pct_device")
public class FailedDeviceQA implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "value", columnDefinition = "VARCHAR(1000)")
	private String value;
	
	@Column(name = "exception", columnDefinition = "VARCHAR(1000)")
	private String exception;
	
	@Column(name="created_at")
	private Instant createdAt;
}
