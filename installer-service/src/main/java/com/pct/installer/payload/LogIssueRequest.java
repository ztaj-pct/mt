package com.pct.installer.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pct.common.constant.InstanceType;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Abhishek on 11/06/20
 */

@Data
@NoArgsConstructor
public class LogIssueRequest {

    @JsonProperty("install_code")
    private String installCode;
    @JsonProperty("sensor_uuid")
    private String sensorUuid;
    @JsonProperty("issue_type")
    private String issueType;
    @JsonProperty("reason_code")
    private String reasonCode;
    @JsonProperty("datetime_rt")
    private String datetimeRT;
    @JsonProperty("comment")
    private String comment;
    private String data;
    private String status;
    @JsonProperty("sensor_id")
    private String sensorId;
    @JsonProperty("issue_uuid")
    private String issueUuid;
    

}
