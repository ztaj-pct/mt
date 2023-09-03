package com.pct.common.payload;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pct.common.dto.AttributeValueResposneDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Abhishek on 11/06/20
 */

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GatewayDetailsBean {

    @JsonProperty("gateway_uuid")
    private String gatewayUuid;
    
    @JsonProperty("gateway_product_code")
    private String gatewayProductCode;
    
    @JsonProperty("gateway_product_name")
    private String gatewayProductName;
    
    @JsonProperty("eligible_asset_type")
    private String eligibleAssetType;
    
    @JsonProperty("is_blocker")
    private boolean is_blocker;
    
    @JsonProperty("attributes")
    private List<AttributeValueResposneDTO> gatewayAttribute;
    
    @JsonProperty("gateway_install_instructions")
    private List<InstallInstructionBean> gatewayInstallInstructions;
    
    @JsonProperty("gateway_reason_codes")
    private Map<String, List<ReasonCodeBean>> gatewayReasonCodes;
   
}
