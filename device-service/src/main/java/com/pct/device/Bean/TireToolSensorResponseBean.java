package com.pct.device.Bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class TireToolSensorResponseBean {
	
	private Long id;
	
	private String uuid;
	
	private String sensorLocation;
	
	private String sensorId;
	
	private String sensorTypeDescription;

	@Override
	public String toString() {
		return "TireToolSensor [id=" + id + ", uuid=" + uuid + ", sensorLocation=" + sensorLocation + ", sensorId="
				+ sensorId + ", sensorTypeDescription=" + sensorTypeDescription + "]";
	}
	
}
