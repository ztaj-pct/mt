package com.pct.installer.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pct.common.payload.InstallInstructionBean;
import com.pct.common.payload.ReasonCodeBean;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author Abhishek on 11/06/20
 */

@Data
@Setter
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SensorDetailsBean {

    @JsonProperty("sensor_uuid")
    private String sensorUuid;
    @JsonProperty("sensor_product_code")
    private String sensorProductCode;
    @JsonProperty("sensor_product_name")
    private String sensorProductName;
    @JsonProperty("sensor_reason_codes")
    private Map<String, List<ReasonCodeBean>> sensorReasonCodes;
    @JsonProperty("sensor_install_instructions")
    private List<InstallInstructionBean> sensorInstallInstructions;
}
