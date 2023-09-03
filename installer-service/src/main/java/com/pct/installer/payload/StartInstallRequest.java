package com.pct.installer.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Abhishek on 01/05/20
 */

@Data
@NoArgsConstructor
@ToString
public class StartInstallRequest {

    @JsonProperty("asset_uuid")
    private String assetUuid;
    @JsonProperty("device_id")
    private String deviceID;
    @JsonProperty("install_uuid")
    private String installUuid;
    @JsonProperty("datetime_rt")
    private String datetimeRT;
    @JsonProperty("app_version")
    private String appVersion;
}
