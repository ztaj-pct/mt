package com.pct.common.payload;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Abhishek on 02/07/20
 */

@Data
@Getter
@Setter
@NoArgsConstructor
public class SensorUpdateRequest {

    private String sensorUuid;
    private String status;
    private String updatedOn;
    private String logUUId;
    
}
