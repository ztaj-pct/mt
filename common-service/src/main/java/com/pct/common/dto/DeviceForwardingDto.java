package com.pct.common.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.model.Device;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)

public class DeviceForwardingDto {
	private Long id;
	private String uuid;
	private Device device;
	private String type;
	private String url;

	public DeviceForwardingDto() {
		super();
	}

	public DeviceForwardingDto(Long id, String uuid, Device device, String type, String url) {
		super();
		this.id = id;
		this.uuid = uuid;
		this.device = device;
		this.type = type;
		this.url = url;
	}

	public DeviceForwardingDto(String uuid, Device device, String type, String url) {
		super();
		this.uuid = uuid;
		this.device = device;
		this.type = type;
		this.url = url;
	}

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

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
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
}
