package com.pct.common.constant;

public enum SensorPosition {
	
	Default("");
	 
	 
	 private final String value;
	 
	 SensorPosition(String value) {
	        this.value = value;
	    }

	    public String getValue() {
	        return this.value;
	    }
	    
	    
	    public static final SensorPosition getSensorPositionInSearch(String value) {
			if (Default.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
				return SensorPosition.Default;
			}
			return null;
	    }

}
