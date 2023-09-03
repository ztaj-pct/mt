package com.pct.organisation.payload;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.pct.common.constant.OrganisationRole;
import com.pct.common.model.CustomerForwardingGroupMapper;
import com.pct.common.model.Location;
import com.pct.common.model.Organisation;
import com.pct.common.model.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganisationListPayload {
	private Long id;

	private Instant createdAt;

	private Instant updatedAt;

	private String organisationName;

	private Boolean isActive;

	private Boolean maintenanceMode = false;

	private String accountNumber;

	private String shortName;

	private String uuid;

	private Boolean isAssetListRequired;

	private User createdBy;

	private User updatedBy;

	private String previousRecordId;

	private List<Organisation> accessList;

	private String epicorAccountNumber;

	private Set<OrganisationRole> organisationRole;

	private List<Organisation> resellerList;

	private List<Organisation> installerList;

	private List<Organisation> maintenanceList;

	private List<CustomerForwardingGroupMapper> forwardingGroupMappers;

	private List<Location> locations;
}
