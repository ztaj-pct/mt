package com.pct.common.payload;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductMasterRequest {
	

	
    @JsonProperty("product_code")
    private String productCode;
    
    @JsonProperty("product_name")
    private String productName;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("subtype")
    private String subtype;
    
    @JsonProperty("is_blocker")
    private boolean isBlocker;
    
    

	
    
}