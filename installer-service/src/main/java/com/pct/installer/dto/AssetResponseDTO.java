package com.pct.installer.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class AssetResponseDTO {
    private String assetUuid;
    private String assignedName;
    private String status;
    private String category;
    private String vin;
    private String can;
    private ManufacturerDetailsDTO manufacturerDetails;
    private String eligibleGateway;
    private String displayName;
    private String datetimeCreated;
    private String datetimeUpdated;
    private String companyName;
    private Boolean isVinValidated;
    private String comment;
    private String imei;
    private String installed;
}
