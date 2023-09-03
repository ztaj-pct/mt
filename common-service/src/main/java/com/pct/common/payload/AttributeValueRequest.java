package com.pct.common.payload;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.GatewayType;
import com.pct.common.model.DateAudit;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AttributeValueRequest  implements Serializable {

   
    
    @JsonProperty("device_id")
     private String deviceId;
    
    @JsonProperty("type")
    private GatewayType type;

    @JsonProperty("attribute_uuid")
    private String attributeUUID;

    @JsonProperty("value")
    private String value;

    @JsonProperty("status")
    private String status;

}
