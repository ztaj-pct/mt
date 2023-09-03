package com.pct.device.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class CellularDetailPayload {
	private String cellular;
	private String carrierId;
	private String serviceNetwork;
	private String serviceCountry;
	private String countryCode;
	private String phone;
	private String iccid;
    private String imsi;
    private String imei;
}
