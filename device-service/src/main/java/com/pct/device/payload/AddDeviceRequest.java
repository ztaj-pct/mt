package com.pct.device.payload;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
public class AddDeviceRequest {
	
	 	@JsonProperty("son")
	    private String salesforceOrderNumber;

	    @JsonProperty("salesforce_account_name")
	    private String salesforceAccountName;

	    @JsonProperty("salesforce_account_id")
	    private String salesforceAccountId;

	    @JsonProperty("epicor_order_number")
	    private String epicorOrderNumber;
	    

	    @JsonProperty("product_short_name")
	    private String productShortName;

	    @JsonProperty("product_code")
	    private String productCode;

	    @JsonProperty("quantity_shipped")
	    private String quantityShipped;

	    @JsonProperty("imei")
	    private String imei;


}
