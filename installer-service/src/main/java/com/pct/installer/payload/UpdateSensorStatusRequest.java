package com.pct.installer.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pct.common.constant.InstanceType;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Abhishek on 04/05/20
 */

@Data
@NoArgsConstructor
public class UpdateSensorStatusRequest {

    @JsonProperty("install_uuid")
    private String installUuid;
    @JsonProperty("sensor_uuid")
    private String sensorUuid;
    @JsonProperty("status")
    private String status;
    private String data;
    @JsonProperty("datetime_rt")
    private String datetimeRT;
    
 
    
}
