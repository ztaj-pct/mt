package com.pct.device.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class AssetVinSearchDTO {
    private String vin;
    private String make;
    private String model;
    private String manufacturer;
    private String modelYear;
    private String category;
    private String bodyType;
    private String length;
    private Integer numberOfAxles;

}
