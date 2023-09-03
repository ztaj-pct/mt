package com.pct.device.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class AssetDTO {
    private Long id;
    private String assignedName;
    private String gatewayEligibility;
    private String vin;
    private String year;
    private String manufacturer;
    private String accountNumber;
    private String category;
    private String status;
    private String config;
    private String createdBy;
    private String updatedBy;
    private Instant dateCreated;
    private Instant dateUpdated;
    private Boolean isVinValidated;
    private String comment;
}
