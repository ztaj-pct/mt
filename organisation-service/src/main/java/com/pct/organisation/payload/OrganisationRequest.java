package com.pct.organisation.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationRequest {

	private Long id;
    private String OrganisationName;
    private String uuid;
	
   
    
   
}
