package com.pct.installer.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pct.common.constant.AssetStatus;
import com.pct.common.constant.GatewayStatus;
import com.pct.common.constant.InstanceType;
import com.pct.common.payload.UpdateGatewayAssetStatusRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpdateSensorStatusWithInstanceRequest {
    @JsonProperty("install_uuid")
    private String installUuid;
    @JsonProperty("sensor_uuid")
    private String sensorUuid;
    private String status;
    private String data;
    @JsonProperty("datetime_rt")
    private String datetimeRT;
    @JsonProperty("sensor_id")
    private String sensorId;
    private String position;
    private String type;
    @JsonProperty("sensor_temperature")
    private String sensorTemperature;
    @JsonProperty("sensor_pressure")
    private String sensorPressure;

}
