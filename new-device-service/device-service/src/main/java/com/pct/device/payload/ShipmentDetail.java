
package com.pct.device.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ShipmentDetail {

    @JsonProperty("sensor_list")
    private List<SensorList> sensorList = null;
    @JsonProperty("quantity_shipped")
    private Integer quantityShipped;
    @JsonProperty("quantity_ordered")
    private Integer quantityOrdered;
    @JsonProperty("quantity_confirmed")
    private Integer quantityConfirmed;
    @JsonProperty("product_short_name")
    private String productShortName;
    @JsonProperty("product_code")
    private String productCode;
    @JsonProperty("order_item_number")
    private String orderItemNumber;
    @JsonProperty("imei_list")
    private List<String> imeiList;
    @JsonProperty("description")
    private String description;
    @JsonProperty("line_item_id")
    private String lineItemId;
}
