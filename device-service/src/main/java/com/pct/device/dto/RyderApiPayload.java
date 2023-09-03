package com.pct.device.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RyderApiPayload {

	String assetName;
	String imei;
	String imei_last5;
	String vin;
}
