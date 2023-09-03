package com.pct.organisation.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@Getter
@Setter 
public class OrganisationAccessDTO {

	//private OrganisationPayload customer;
    
	private Long id;
    private String OrganisationName;
    private String shortName;
    private String type;
    private Boolean status;
    private String accountNumber;
    private Boolean isAssetListRequired;
    private String uuid;
    private OrganisationPayload customer;
    private List<OrganisationPayload> organisationViewList;
 
    
    
}
