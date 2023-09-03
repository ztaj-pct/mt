package com.pct.device.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.model.Device;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)

public class DeviceForwardingRequest {
	@JsonProperty("product_code")
	private String uuid;

	@JsonProperty("device")
	private Device device;

	@JsonProperty("type")
	private String type;

	@JsonProperty("url")
	private String url;
}
