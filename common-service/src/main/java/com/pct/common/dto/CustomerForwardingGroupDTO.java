package com.pct.common.dto;

import java.io.Serializable;
import java.time.Instant;

import lombok.Data;

@Data
public class CustomerForwardingGroupDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	private String uuid;

	private String name;

	private String description;
	
	private String createdBy;

	private Instant createdOn;

	private String updatedBy;

	private Instant updatedOn;
}
