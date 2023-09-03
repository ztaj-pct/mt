package com.pct.device.dto;

import java.time.Instant;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;



import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.model.User;
import com.pct.device.constant.AssetCategory;
import com.pct.common.constant.AssetStatus;

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
public class AssetDTO {

	    private Long id;
	    private String assignedName;
	    private String deviceEligibility;
	    private String vin;
	    private String year;
	    private String manufacturer;
	    private String accountNumber;
	    private String category;
	    private String status;
	    private String config;
	    private String createdBy;
	    private User updatedBy;
	    private Instant dateCreated;
	    private Instant dateUpdated;
	    private Boolean isVinValidated;
	    private String comment;

}
