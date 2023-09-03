package com.pct.common.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrganisationDtoForInventory {

	private Long id;
	private String accountNumber;
	private String organisationName;
	private Boolean isAssetListRequired;
	private Boolean maintenanceMode;

}
