package com.pct.installer.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class InventoryListDTO {
    private boolean requireAssetList;
    private List<InventoryListAssetDTO> assets;
    private List<InventoryListGatewayDTO> gateways;
    private List<InventoryListBeaconDTO> beacon;
    private String accountNumber;
    private String customerName;
    private boolean canMaintenance;
}
