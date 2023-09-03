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
public class TireToolSensorBean {
	private String sensorLocation;
    private String sensorId;
    private String sensorTypeDescription;
    
	@Override
	public String toString() {
		return "TireToolSensorBean [sensorLocation=" + sensorLocation + ", sensorId=" + sensorId
				+ ", sensorTypeDescription=" + sensorTypeDescription + "]";
	}

    
}
