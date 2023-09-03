package com.pct.device.dto;

import java.io.Serializable;
import java.time.Instant;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class DeviceIgnoreForwardingRuleDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	private String uuid;

	private String type;

	private String url;

	private String customerForwardingRuleUuid;

	private String deviceImei;

	private String createdBy;

	private Instant createdOn;

	private String updatedBy;

	private Instant updatedOn;
}
