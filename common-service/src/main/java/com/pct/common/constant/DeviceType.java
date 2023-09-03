package com.pct.common.constant;

public enum DeviceType {
	
//	BEACON, GATEWAY, SENSOR;
	BEACON("Beacon"), GATEWAY("Gateway"), SENSOR("Sensor");
	
	private final String value;

	DeviceType(String value) {
        this.value = value;
    }


	public static DeviceType getValue(String val) {
		if (val.equalsIgnoreCase(DeviceType.BEACON.toString())) {
			return DeviceType.BEACON;
		} else if (val.equalsIgnoreCase(DeviceType.GATEWAY.toString())) {
			return DeviceType.GATEWAY;
		} else if (val.equalsIgnoreCase(DeviceType.SENSOR.toString())) {
			return DeviceType.SENSOR;
		}
		return null;

	}
	 public String getDeviceValue() {
	        return this.value;
	    }
}
