package com.pct.common.payload;

import java.io.Serializable;

import com.pct.common.constant.AssetStatus;
import com.pct.common.constant.DeviceStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Abhishek on 13/05/20
 */

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGatewayAssetStatusRequest implements Serializable{

    private String gatewayUuid;
    private String assetUuid;
    private DeviceStatus gatewayStatus;
    private AssetStatus assetStatus;
    private String logUUId;
}
