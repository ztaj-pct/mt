package com.pct.device.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Abhishek on 26/05/20
 */

@Data
@NoArgsConstructor
public class AssetDetail {

    @JsonProperty("product_short_name")
    private String productShortName;

    @JsonProperty("product_code")
    private String productCode;

    @JsonProperty("quantity_shipped")
    private String quantityShipped;

    @JsonProperty("imei_list")
    private List<String> imeiList;


    @JsonProperty("sensor_list")
    private List<SensorProduct> sensorList;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
}
