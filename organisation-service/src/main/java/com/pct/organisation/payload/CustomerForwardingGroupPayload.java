package com.pct.organisation.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomerForwardingGroupPayload {
	
	@JsonProperty("id")
	private Long id;
	
	@JsonProperty("uuid")
	private String uuid;
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("description")
	private String description;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
