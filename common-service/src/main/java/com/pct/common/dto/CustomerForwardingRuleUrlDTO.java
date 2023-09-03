package com.pct.common.dto;

import java.io.Serializable;
import java.time.Instant;

import lombok.Data;

@Data
public class CustomerForwardingRuleUrlDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	private String uuid;

	private String ruleName;

	private String format;
	
	private String type;
	
	private String kafkaTopicName;

	private String endpointDestination;

	private String description;

	private String createdBy;

	private Instant createdOn;

	private String updatedBy;

	private Instant updatedOn;
}
