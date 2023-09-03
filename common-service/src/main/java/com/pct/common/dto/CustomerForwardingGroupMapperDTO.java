package com.pct.common.dto;

import java.io.Serializable;
import java.time.Instant;

import lombok.Data;

@Data
public class CustomerForwardingGroupMapperDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String customerForwardingGroupUuid;

	private String organisationUuid;
	
	private String createdBy;

	private Instant createdOn;

	private String updatedBy;

	private Instant updatedOn;

}
