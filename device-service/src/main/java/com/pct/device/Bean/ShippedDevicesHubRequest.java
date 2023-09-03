package com.pct.device.Bean;

import com.fasterxml.jackson.annotation.JsonProperty;
//import com.pct.device.payload.ShippedDevice;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Abhishek on 01/07/20
 */

@Data
@NoArgsConstructor
public class ShippedDevicesHubRequest {

    @JsonProperty("salesforce_order_number")
    private String salesforceOrderNumber;
    @JsonProperty("salesforce_account_id")
    private String salesforceAccountId;
    @JsonProperty("epicor_order_number")
    private String epicorOrderNumber;
    @JsonProperty("packing_slip_number")
    private String packingSlipNumber;
    @JsonProperty("shipment_details")
    private List<HubAssetDetail> hubAssetDetails;
}
