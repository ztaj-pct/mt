package com.pct.device.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Abhishek on 26/05/20
 */

@Data
@NoArgsConstructor
@ToString
public class ShipmentDetailsRequest {

    @JsonProperty("salesforce_order_number")
    private String salesforceOrderNumber;

    @JsonProperty("salesforce_account_name")
    private String salesforceAccountName;

    @JsonProperty("salesforce_account_id")
    private String salesforceAccountId;

    @JsonProperty("epicor_order_number")
    private String epicorOrderNumber;

    @JsonProperty("asset_details")
    private List<AssetDetail> assetDetails;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
}
