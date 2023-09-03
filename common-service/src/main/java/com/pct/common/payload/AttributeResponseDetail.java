package com.pct.common.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttributeResponseDetail {
    private String name;
    private String attributeUUID;
    private String threshHoldValue;
    private String value;
   
}
