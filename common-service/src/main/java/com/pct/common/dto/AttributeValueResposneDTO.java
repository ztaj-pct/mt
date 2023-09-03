package com.pct.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AttributeValueResposneDTO {
	
	
	    private String attribute_uuid;
	
	    @JsonProperty("is_applicable")
	    private boolean applicable;
	    
	    @JsonProperty("attribute_name")
	    private String attributeName;
	    
	    @JsonProperty("threshold_value")
	    private String thresholdValue;
	    
	   
	    
	    
	    
	   
	    
	   

}
