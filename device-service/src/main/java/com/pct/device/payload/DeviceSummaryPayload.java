package com.pct.device.payload;

import java.time.Instant;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Abhishek on 16/07/20
 */


@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
public class DeviceSummaryPayload {
	
//    private String createdFirstName;
//    private String createdLastName;
    private String companyName;
//    private String updatedFirstName;
//    private String updatedLastName;
    private Integer count;
//    private Instant createdAt;
//    private Instant updatedAt;
    private Long companyId;
}