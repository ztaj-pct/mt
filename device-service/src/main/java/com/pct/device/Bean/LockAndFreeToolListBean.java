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
public class LockAndFreeToolListBean {
	  private String imei;
	  private String eventTimestamp;
	  private String eventType;
	  
	@Override
	public String toString() {
		return "LockAndFreeToolListBean [imei=" + imei + ", eventTimestamp=" + eventTimestamp + ", eventType="
				+ eventType + "]";
	}
	
	
    
    
}
