package com.pct.device.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Abhishek on 26/05/20
 */

@Data
@NoArgsConstructor
public class DataList {

    @JsonProperty("product_code")
    private String productCode;

    @JsonProperty("product_name")
    private String productName;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
}
