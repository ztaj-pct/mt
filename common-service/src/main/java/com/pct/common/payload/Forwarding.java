package com.pct.common.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.model.Device;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Forwarding {

	@JsonProperty("type")
	private String type;

	@JsonProperty("url")
	private String url;

	@JsonProperty("uuid")
	private String uuId;
	
	@JsonProperty("id")
	private long id;
	
	@JsonProperty("forwarding_rule_url_uuid")
	private String forwardingRuleUrlUuid;
	
}
