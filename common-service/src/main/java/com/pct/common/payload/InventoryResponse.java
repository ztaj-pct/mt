package com.pct.common.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author Abhishek on 01/05/20
 */

@Data
@NoArgsConstructor
public class InventoryResponse {

    private Map<String, Integer> assets;
    private Map<String, Integer> gateways;
}
