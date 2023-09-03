package com.pct.device.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class InventoryListDTO {
    private List<InventoryListAssetDTO> assets;
    private List<InventoryListGatewayDTO> gateways;
    private String accountNumber;
    private String customerName;
}
