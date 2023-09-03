package com.pct.organisation.payload;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CustomerForwardingRuleUrlPayload implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("rule_name")
	private String ruleName;

	@JsonProperty("format")
	private String format;

	@JsonProperty("type")
	private String type;

	@JsonProperty("endpoint_destination")
	private String endpointDestination;

	@JsonProperty("description")
	private String description;

	@JsonProperty("kafka_topic_name")
	private String kafkaTopicName;
	
}
