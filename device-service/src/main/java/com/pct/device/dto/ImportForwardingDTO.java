package com.pct.device.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportForwardingDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String deviceId;
	private String type;
	private String url;

}
