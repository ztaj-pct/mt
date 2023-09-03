package com.pct.device.Bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class LockAndFreeToolDataResponseBean {
	
	private Long id;
	
	private String uuid;

	private String imei;
	
	private String eventTimestamp;
	
	private String eventType;

	@Override
	public String toString() {
		return "LockAndFreeToolDataResponseBean [id=" + id + ", uuid=" + uuid + ", imei=" + imei + ", eventTimestamp="
				+ eventTimestamp + ", eventType=" + eventType + "]";
	}

    
}
