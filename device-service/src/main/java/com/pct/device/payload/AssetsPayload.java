package com.pct.device.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AssetsPayload {

    private Long id;
    private String uuid;
    private String assignedName;
    private String eligibleGateway;
    private String vin;
    private String category;
    private String year;
    private String manufacturer;
    private CompanyPayload company;
    private String status;
    private Boolean isVinValidated;
    private String noOfTires;
    private String noOfAxel;
    private String externalLength;
    private String assetNickName;
    private String tag;
    private String doorType;
    private String comment;
    
}
