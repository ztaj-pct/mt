package com.pct.organisation.payload;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddOrganisationResponse {

	private Long id;
	private String OrganisationName;
	private String shortName;
	private Boolean status;
//	private OrganisationType type;
	private String accountNumber;
	private String uuid;
	private String epicorId;
	private String previousRecordId;
	
}
