package com.pct.device.Bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class BatTestResponseBean {
	
	private Long id;
	
	private String uuid;

	private String tpmsMac;
	
	private String voltage;

	@Override
	public String toString() {
		return "BatTest [id=" + id + ", uuid=" + uuid + ", tpmsMac=" + tpmsMac + ", voltage=" + voltage + "]";
	}
    
    
}
