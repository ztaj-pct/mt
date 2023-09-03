package com.pct.device.Bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class SpareToolSensorResponseBean {
	
	private Long id;
	
	private String uuid;

	private String sensorMac;
	
	private String sensorType;
	
	private String sensorTypeDescription;
	
	private String sensorLocation;

	@Override
	public String toString() {
		return "SpareToolSensorResponseBean [id=" + id + ", uuid=" + uuid + ", sensorMac=" + sensorMac + ", sensorType="
				+ sensorType + ", sensorTypeDescription=" + sensorTypeDescription + ", sensorLocation=" + sensorLocation
				+ "]";
	}

	
	

    
    
}
