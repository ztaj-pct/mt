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
public class ProductionToolBean {
    private String toolName;
    private String toolRevision;
    private String toolIpAddress;
    private String toolOperator;
    private String eventTimestamp;
    private String eventType;
    private EventContentBean eventContent;
    
	@Override
	public String toString() {
		return "ProductionToolBean [toolName=" + toolName + ", toolRevision=" + toolRevision + ", toolIpAddress="
				+ toolIpAddress + ", toolOperator=" + toolOperator + ", eventTimestamp=" + eventTimestamp
				+ ", eventType=" + eventType + ", eventContent=" + eventContent + "]";
	}
    
    
}
