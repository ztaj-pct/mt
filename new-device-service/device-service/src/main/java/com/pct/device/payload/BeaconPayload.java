package com.pct.device.payload;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class BeaconPayload {
	
	
	   @JsonProperty("product_short_name")
	    private String productShortName;

	    @JsonProperty("product_code")
	    private String productCode;

	    @JsonProperty("quantity_shipped")
	    private String quantityShipped;
	   
	    @JsonProperty("mac_Address")
	    private String macAddress;
	    
	   
	

}
