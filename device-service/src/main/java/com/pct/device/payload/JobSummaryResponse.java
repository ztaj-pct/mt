package com.pct.device.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author Abhishek on 16/04/20
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSummaryResponse {

    private Map<String, Integer> inventoryReadyForInstallList;
    private Map<String, Integer> assetsApprovedForInstallList;
    private Map<String, Integer> installationInProgressList;
}
