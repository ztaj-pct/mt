package com.pct.device.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
public class GatewaySummaryPayload {

	private String organisationName;
	private Integer count;
	private Long organisationId;
	private Long smartSeven;
	private Long traillerNet;
	private Long smartPair;
	private Long stealthNet;
	private Long sabre;
	private Long freightLa;
	private Long arrowL;
	private Long freightL	;
	private Long cutlassL;
	private Long dagger67Lg;
	private Long smart7;
	private Long katanaH;

}
