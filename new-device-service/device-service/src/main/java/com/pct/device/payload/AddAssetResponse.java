package com.pct.device.payload;

import com.pct.common.model.Asset;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Abhishek on 08/02/21
 */
@Data
public class AddAssetResponse {

    private Map<String, List<String>> errors;
    private AssetsPayload assetPayload;
}
