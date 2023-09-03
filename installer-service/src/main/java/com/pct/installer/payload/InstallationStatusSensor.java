package com.pct.installer.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Abhishek on 04/05/20
 */

@Data
@NoArgsConstructor
public class InstallationStatusSensor {

    @JsonProperty("sensor_uuid")
    private String sensorUuid;
    @JsonProperty("sensor_status")
    private String sensorStatus;
    @JsonProperty("sensor_data")
    private String sensorData;
    @JsonProperty("sensor_datetime_rt")
    private String sensorDatetimeRT;
}
