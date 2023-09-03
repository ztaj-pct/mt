package com.pct.organisation.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomerForwardingRulePayload {
	
	@JsonProperty("id")
	private Long id;
	
	@JsonProperty("uuid")
	private String uuid;
	
	@JsonProperty("type")
	private String type;

	@JsonProperty("url")
	private String url;
	
	@JsonProperty("forwarding_rule_url_uuid")
	private String forwardingRuleUrlUuid;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getForwardingRuleUrlUuid() {
		return forwardingRuleUrlUuid;
	}

	public void setForwardingRuleUrlUuid(String forwardingRuleUrlUuid) {
		this.forwardingRuleUrlUuid = forwardingRuleUrlUuid;
	}

}
