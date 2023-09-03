package com.pct.organisation.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomerForwardingGroupMapperPayload {
	
	@JsonProperty("customer_forwarding_group_uuid")
	private String customerForwardingGroupUuid;
	
	@JsonProperty("organisation_uuid")
	private String organisationUuid;

	public String getCustomerForwardingGroupUuid() {
		return customerForwardingGroupUuid;
	}

	public void setCustomerForwardingGroupUuid(String customerForwardingGroupUuid) {
		this.customerForwardingGroupUuid = customerForwardingGroupUuid;
	}

	public String getOrganisationUuid() {
		return organisationUuid;
	}

	public void setOrganisationUuid(String organisationUuid) {
		this.organisationUuid = organisationUuid;
	}
	
	
}
