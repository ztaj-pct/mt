package com.pct.device.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GatewayUpdateDTO {
		
	private String id;
	private Boolean status = true;
	private String message = "SUCCESS";
	
	public GatewayUpdateDTO(String id) {
		super();
		this.id = id;
	}
	
}
