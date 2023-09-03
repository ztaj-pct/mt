package com.pct.device.dto;

import java.time.Instant;
import java.util.Date;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.device.constant.DeviceStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class AssetResponseDTO {
    private String assetUuid;
    private Long id;
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
    private String installationCode;
    private String installationStatus;
    private String deviceUuid;
    private Instant installationDate;
    private String noOfTires;
    private String noOfAxel;
    private String externalLength;
    private String assetNickName;
    private String tag;
    private String doorType;
}
