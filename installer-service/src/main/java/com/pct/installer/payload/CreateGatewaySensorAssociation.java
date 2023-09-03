package com.pct.installer.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Ashish on 21/07/2022
 */

@Data
@Setter
@Getter
@NoArgsConstructor
public class CreateGatewaySensorAssociation {

    @JsonProperty("product_code")
    private String productCode;
    
    @JsonProperty("product_name")
    private String productName;
    
    @JsonProperty("gateway_uuid")
    private String gatewayUuid;
    
    @JsonProperty("install_uuid")
    private String installUuid;
    
    @JsonProperty("mac_address")
    private String macAddress;
    
    @JsonProperty("sensor_status")
    private String sensorStatus;
}
