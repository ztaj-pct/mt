package com.pct.common.constant;

public enum GatewayType {
	
	  GATEWAY("Gateway"), BEACON("Beacon");

	
	private final String value;

	GatewayType(String value) {
        this.value = value;
    }

	 public final static GatewayType getGatewayType(String type) {

	        if (type.equalsIgnoreCase(GATEWAY.getValue())) {
	            return GATEWAY;
	        } else if (type.equalsIgnoreCase(BEACON.getValue())) {
	            return BEACON;
	        }
	        return null;
	    }
	
    public String getValue() {
        return this.value;
    }
}
