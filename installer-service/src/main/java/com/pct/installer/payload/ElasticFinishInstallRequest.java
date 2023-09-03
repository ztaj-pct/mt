package com.pct.installer.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Abhishek on 16/04/21
 */
@Data
@NoArgsConstructor
public class ElasticFinishInstallRequest {

    private String deviceId;
    private String assetId;
    private String vin;
}
