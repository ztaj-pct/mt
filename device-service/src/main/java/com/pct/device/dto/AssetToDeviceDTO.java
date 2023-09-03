package com.pct.device.dto;

import java.sql.Timestamp;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetToDeviceDTO {
	
	 public String gateway_id;
	 public String asset_id;
	 private Timestamp install_timestamp;
	 

}
