package com.pct.installer.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.pct.common.dto.AttributeValueResposneDTO;
import com.pct.common.payload.AttributeResponse;
import com.pct.common.payload.InstallInstructionBean;
import com.pct.common.payload.ReasonCodeBean;

import java.util.List;
import java.util.Map;

/**
 * @author Abhishek on 11/06/20
 */

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DeviceDetailsBean {

    @JsonProperty("device_uuid")
    private String deviceUuid;
    
    @JsonProperty("device_product_code")
    private String deviceProductCode;
    
    @JsonProperty("device_product_name")
    private String deviceProductName;
    
    @JsonProperty("eligible_asset_type")
    private String eligibleAssetType;
    
    @JsonProperty("is_blocker")
    private boolean is_blocker;
    
    @JsonProperty("attributes")
    private List<AttributeValueResposneDTO> deviceAttribute;
    
    @JsonProperty("device_install_instructions")
    private List<InstallInstructionBean> deviceInstallInstructions;
    
    @JsonProperty("gateway_reason_codes")
    private Map<String, List<ReasonCodeBean>> deviceReasonCodes;
   
}
