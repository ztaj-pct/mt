package com.pct.installer.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Abhishek on 04/05/20
 */

@Data
@NoArgsConstructor
public class FinishInstallRequest {

    @JsonProperty("install_uuid")
    private String installUuid;
    @JsonProperty("is_verified")
    private boolean isVerified;
    private String status;
    @JsonProperty("reason_code")
    private String reasonCode;
    @JsonProperty("datetime_rt")
    private String datetimeRT;
}
