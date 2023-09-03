package com.pct.device.payload;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Abhishek on 24/02/21
 */

@Data
public class GetInstallHistoryByAssetUuids {

    List<String> assetUuids;
    Map<String, String> filterValues;
}
