package com.pct.installer.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Abhishek on 12/06/20
 */

@Data
@NoArgsConstructor
public class SensorDetailsResponse {

    @JsonProperty("gateway_uuid")
    private String gatewayUuid;
    @JsonProperty("sensor_list")
    private List<SensorDetailsBean> sensorList;
}
