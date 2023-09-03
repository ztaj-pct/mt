package com.pct.device.payload;

import lombok.Data;

import java.util.List;
import java.util.Map;
 
@Data
public class AddAssetResponse {

    private Map<String, List<String>> errors;
    private AssetsPayload assetPayload;
}
