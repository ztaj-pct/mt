package com.pct.device.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttributeResponse {

	
    private String name;
    private String attribute_uuid;
    private String threshold_Value;
    private ProductMasterResponse product_response;

}
