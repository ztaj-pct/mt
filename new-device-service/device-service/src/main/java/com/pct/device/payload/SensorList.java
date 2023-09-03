
package com.pct.device.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SensorList {

    @JsonProperty("sensor_product_name")
    private String sensorProductName;
    @JsonProperty("sensor_product_code")
    private String sensorProductCode;
}
