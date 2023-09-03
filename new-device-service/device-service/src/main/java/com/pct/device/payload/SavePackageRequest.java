package com.pct.device.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author dhruv
 *
 */
@Data
@NoArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SavePackageRequest {

    private String packageName;

    private String binVersion;

    private String appVersion;

    private String mcuVersion;
	
    private String bleVersion;
	
    private String config1;
	
    private String config2;
	
    private String config3;
	
    private String config4;

}
