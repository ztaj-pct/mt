
package com.pct.device.Bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class HubAssetDetail {

    @JsonProperty("product_code")
    private String productCode;
    @JsonProperty("quantity_shipped")
    private String quantityShipped;
    @JsonProperty("order_item_number")
    private String orderItemNumber;
    @JsonProperty("imei_list")
    private List<String> imeiList;
}
