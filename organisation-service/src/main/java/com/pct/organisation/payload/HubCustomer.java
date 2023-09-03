package com.pct.organisation.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HubCustomer {

	    @JsonProperty("salesforce_account_id")
	    public String salesforceAccountId;
	    @JsonProperty("epicor_account_id")
	    public String epicorAccountId;
	    @JsonProperty("account_name")
	    public String accountName;
	
}
