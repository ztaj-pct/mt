package com.pct.device.payload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class BeaconDetailsRequest {

	 @JsonProperty("salesforce_order_number")
	    private String salesforceOrderNumber;

	    @JsonProperty("salesforce_account_name")
	    private String salesforceAccountName;

	    @JsonProperty("salesforce_account_id")
	    private String salesforceAccountId;

	    @JsonProperty("epicor_order_number")
	    private String epicorOrderNumber;

	    @JsonProperty("beacon_details")
	    private List<BeaconPayload> beaconDetails;

	    @JsonIgnore
	    private Map<String, Object> additionalProperties = new HashMap<>();
}
