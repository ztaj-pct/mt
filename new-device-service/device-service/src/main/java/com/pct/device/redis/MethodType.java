package com.pct.device.redis;

import lombok.Getter;

@Getter
public enum MethodType {
	POST("POST"), 
	PUT("PUT"), 
	GET("GET"),
	DELETE("DELETE"), 
	PATCH("PATCH");
	
	private MethodType(String type) {
		this.type = type;
	}
	private String type;
}
