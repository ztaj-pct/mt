package com.pct.common.payload;

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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeviceAssetStatusRequest {

    private String deviceUuid;
    private String assetUuid;
    private DeviceStatus deviceStatus;
    private AssetStatus assetStatus;
    private String logUUId;
    
}
