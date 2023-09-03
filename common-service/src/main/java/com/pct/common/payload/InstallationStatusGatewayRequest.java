package com.pct.common.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@Data
@NoArgsConstructor
public class InstallationStatusGatewayRequest {
    private String installUuid;
    
    @JsonProperty("gateway_uuid")
    private String gatewayUuid;
    
    private String status;
    private String data;
    private String datetimeRt;
   
}
