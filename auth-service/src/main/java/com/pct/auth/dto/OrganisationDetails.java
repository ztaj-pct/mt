package com.pct.auth.dto;import java.util.Set;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.constant.OrganisationRole;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@Getter
@Setter
public class OrganisationDetails {
	
	private String accountNumber;
	private String organizationName;
	private String uuid;
	private Long id;
	private boolean isActive;
	private Set<OrganisationRole> organisationRole;

}
