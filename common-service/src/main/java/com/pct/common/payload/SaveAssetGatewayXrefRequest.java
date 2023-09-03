package com.pct.common.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Abhishek on 08/05/20
 */

@Data
@NoArgsConstructor
public class SaveAssetGatewayXrefRequest {

    @JsonProperty("asset_uuid")
    private String assetUuid;
    private String imei;
    @JsonProperty("is_active")
    private Boolean isActive;
    @JsonProperty("datetime_rt")
    private String datetimeRT;
    @JsonProperty("logUUId")
    private String logUUId;
}
