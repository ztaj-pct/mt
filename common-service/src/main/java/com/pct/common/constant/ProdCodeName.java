package com.pct.common.constant;

public enum ProdCodeName {
	
	 Door_Sensor("77-S108");
	 
	 
	 private final String value;
	 
	 ProdCodeName(String value) {
	        this.value = value;
	    }

	    public String getValue() {
	        return this.value;
	    }
	    
	    
	    public static final ProdCodeName getProductCodeInSearch(String value) {
			if (Door_Sensor.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
				return ProdCodeName.Door_Sensor;
			}
			return null;
	    }
}
