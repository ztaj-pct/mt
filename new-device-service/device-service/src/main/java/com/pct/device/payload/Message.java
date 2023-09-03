
package com.pct.device.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Message {

    @JsonProperty("shipment_details")
    private List<ShipmentDetail> shipmentDetails;
    @JsonProperty("salesforce_order_number")
    private String salesforceOrderNumber;
    @JsonProperty("salesforce_account_name")
    private String salesforceAccountName;
    @JsonProperty("salesforce_account_id")
    private String salesforceAccountId;
    @JsonProperty("epicor_order_number")
    private String epicorOrderNumber;
}
