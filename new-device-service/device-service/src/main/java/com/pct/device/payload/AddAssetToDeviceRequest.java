package com.pct.device.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * @author Abhishek on 05/02/21
 */

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AddAssetToDeviceRequest {

    private String assetId;
    private String assetType;
    private String customer;
    private String vin;
    private String make;
    private String model;
    private String year;
    private int milesBeforeInstall;
    private long installTimestamp;
    private String deviceId;
    private String comment;
}
