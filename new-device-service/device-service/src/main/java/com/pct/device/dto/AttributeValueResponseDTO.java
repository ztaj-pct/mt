package com.pct.device.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttributeValueResponseDTO {
	
	    private String device_id;

	    private String power_source_name;
	    
	    private String actual_value;
	     
	    //private GatewayType type;

	    private String threshold_value;

	    private String status;

}
