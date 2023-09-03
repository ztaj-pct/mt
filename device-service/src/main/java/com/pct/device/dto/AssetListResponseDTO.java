package com.pct.device.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.constant.AssetCategory;
import com.pct.common.constant.AssetStatus;
import com.pct.common.model.ManufacturerDetails;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class AssetListResponseDTO  {
	
	
	private String assetUuid;
    private String assignedName;
    private AssetStatus status;
    private AssetCategory category;
    private String vin;
    private String can;
    private ManufacturerDetails manufacturerDetails;
    private String eligibleDevice;
    private String datetimeCreated;
    private String datetimeUpdated;
    private String companyName;
    private Boolean isVinValidated;
    private String comment;
    private String year;
}