package com.pct.installer.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Abhishek on 19/06/20
 */

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
public class LogIssueBean {

    private String installCode;
    private String sensorUuid;
    private String sensorProductName;
    private String sensorProductCode;
    private String reasonCode;
    private String reasonCodeDisplayName;
    private String issueType;
    private String comment;
    private String data;
    private String createdOn;
    private String status;
    private String issueUuid;

}
