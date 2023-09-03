package com.pct.device.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CompanyPayload {
	private Long id;
	private String companyName;
	private String shortName;
	private Boolean status;
	private String accountNumber;
	private Boolean isAssetListRequired;
	private String uuid;
}
