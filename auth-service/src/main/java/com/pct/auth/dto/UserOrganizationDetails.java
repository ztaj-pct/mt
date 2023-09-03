package com.pct.auth.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@Getter
@Setter
public class UserOrganizationDetails {

	public Long id;
	public String uuid;
	private List<String> assignedRoles;
	private String defaultLocationUuid;
	private OrganisationDetails organization;
	List<OrganisationDetails> orgAccessList;
	
}
