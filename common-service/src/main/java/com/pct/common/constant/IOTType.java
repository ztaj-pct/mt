package com.pct.common.constant;

public enum IOTType {
	
BEACON("Beacon"), GATEWAY("Gateway"), SENSOR("Sensor");
	
	private final String value;

	IOTType(String value) {
        this.value = value;
    }


	public static IOTType getValue(String val) {
		if (val.equalsIgnoreCase(IOTType.BEACON.toString())) {
			return IOTType.BEACON;
		} else if (val.equalsIgnoreCase(IOTType.GATEWAY.toString())) {
			return IOTType.GATEWAY;
		} else if (val.equalsIgnoreCase(IOTType.SENSOR.toString())) {
			return IOTType.SENSOR;
		}
		return null;

	}
	 public String getIOTTypeValue() {
	        return this.value;
	    }
}
