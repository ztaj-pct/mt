package com.pct.device.Bean;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
@NoArgsConstructor
public class SpareToolSensorBean {
    private String sensorMac;
    private String sensorType;
    private String sensorTypeDescription;
    private String sensorLocation;
	
    @Override
	public String toString() {
		return "SpareToolSensorBean [sensorMac=" + sensorMac + ", sensorType=" + sensorType + ", sensorTypeDescription="
				+ sensorTypeDescription + ", sensorLocation=" + sensorLocation + "]";
	}
    
	
    
    
}
