package com.pct.common.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * @author Abhishek on 20/01/21
 */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@Getter
@Setter
@NoArgsConstructor
public class UpdateAssetToDeviceForInstallationRequest {

    private String assignedName;
    private String vin;
    private String imei;
    private Timestamp installTimestamp;
    private String customerName;
    private String assetType;
    private String make;
    private String model;
    private String year;
}
