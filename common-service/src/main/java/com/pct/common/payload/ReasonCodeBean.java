package com.pct.common.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Abhishek on 12/06/20
 */

@Data
@NoArgsConstructor
public class ReasonCodeBean {

    @JsonProperty("reason_code")
    private String reasonCode;
    @JsonProperty("display_name")
    private String displayName;
}
