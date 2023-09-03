package com.pct.common.dto;

import java.io.Serializable;
import java.time.Instant;

import lombok.Data;

@Data
public class CustomerForwardingRuleDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	private String uuid;

	private String type;

	private String url;

	private String organisationUuid;
	
	private OrganisationDTO organisation;
	
	private String forwardingRuleUrlUuid;
	
	private CustomerForwardingRuleUrlDTO forwardingRuleUrl;
	
	private String createdBy;

	private Instant createdOn;

	private String updatedBy;

	private Instant updatedOn;
}
