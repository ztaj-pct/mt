
package com.pct.device.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HubData {

    @JsonProperty("product_code")
    private String productCode;
    @JsonProperty("product_name")
    private String productName;
}
