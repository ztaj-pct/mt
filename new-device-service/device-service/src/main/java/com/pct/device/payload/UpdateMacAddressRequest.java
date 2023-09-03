package com.pct.device.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Abhishek on 26/05/20
 */

@Data
@NoArgsConstructor
public class UpdateMacAddressRequest {

    private String uuid;
    @JsonProperty("mac_address")
    private String macAddress;
    @JsonProperty("datetime_rt")
    private String datetimeRT;
}
