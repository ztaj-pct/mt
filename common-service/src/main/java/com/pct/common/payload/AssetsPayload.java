package com.pct.common.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetsPayload {

    private Long id;
    private String uuid;
    private String assignedName;
    private String eligibleGateway;
    private String vin;
    private String category;
    private String year;
    private String manufacturer;
    private OrganisationPayload company;
    private String status;
    private Boolean isVinValidated;
    private String comment;

}
