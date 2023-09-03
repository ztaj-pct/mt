package com.pct.installer.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Abhishek on 04/05/20
 */

@Data
@NoArgsConstructor
public class InstallationStatusResponse {

    @JsonProperty("asset_uuid")
    private String assetUuid;
    @JsonProperty("asset_status")
    private String assetStatus;
    @JsonProperty("gateway_uuid")
    private String gatewayUuid;
    @JsonProperty("gateway_status")
    private String gatewayStatus;
    @JsonProperty("gateway_data")
    private String gatewayData;
    @JsonProperty("gateway_datetime_rt")
    private String gatewayDatetimeRT;
    @JsonProperty("sensor_list")
    private List<InstallationStatusSensor> sensorList;
    @JsonProperty("install_status")
    private String installStatus;
}
