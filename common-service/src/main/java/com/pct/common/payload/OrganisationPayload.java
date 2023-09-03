package com.pct.common.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisationPayload {
	private Long id;
	private String companyName;
	private String shortName;
	private String type;
	private Boolean status;
	private String accountNumber;
	private Boolean isAssetListRequired;
	private String uuid;
}
