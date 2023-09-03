package com.pct.device.Bean;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class SpareToolResponseBean {
	
	
	private Long id;
	
	private String uuid;
	
	private String imei;
	
	private List<SpareToolSensorResponseBean> spareToolSensors;

	@Override
	public String toString() {
		return "SpareTool [id=" + id + ", uuid=" + uuid + ", imei=" + imei + ", spareToolSensors=" + spareToolSensors
				+ "]";
	}

    
    
}
