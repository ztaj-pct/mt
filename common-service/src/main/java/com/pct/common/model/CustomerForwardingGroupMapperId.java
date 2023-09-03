package com.pct.common.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CustomerForwardingGroupMapperId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "customer_forwarding_group_uuid", nullable = false, length = 255)
	private String customerForwardingGroupUuid;

	@Column(name = "organisation_uuid", nullable = false, length = 255)
	private String organisationUuid;

}
