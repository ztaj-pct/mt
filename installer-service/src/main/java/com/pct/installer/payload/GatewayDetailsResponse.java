package com.pct.installer.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pct.common.payload.GatewayDetailsBean;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Abhishek on 12/06/20
 */

@Data
@NoArgsConstructor
public class GatewayDetailsResponse {

    @JsonProperty("gateway_details")
    private List<GatewayDetailsBean> gatewayDetailsBean;
}
