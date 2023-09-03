package com.pct.common.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Abhishek on 28/04/20
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAssetForGatewayRequest {

    private String gatewayUuid;
    private String assetUuid;
}
