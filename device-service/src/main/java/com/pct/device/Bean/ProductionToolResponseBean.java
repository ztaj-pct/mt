package com.pct.device.Bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ProductionToolResponseBean {
	
	private Long id;

	private String uuid;
	
	private String toolName;

	private String toolRevision;
	
	private String toolIpAddress;
	
	private String toolOperator;

	private String eventTimestamp;
	
	private String eventType;
	
	@JsonInclude(value = Include.NON_NULL)
	private BatTestResponseBean batTest;
	
	@JsonInclude(value = Include.NON_NULL)
	private SpareToolResponseBean spareTool;
	
	@JsonInclude(value = Include.NON_NULL)
	private TireToolResponseBean tireTool;
	
	@JsonInclude(value = Include.NON_NULL)
	private LockAndFreeToolResponseBean lockTool;
	
	@JsonInclude(value = Include.NON_NULL)
	private LockAndFreeToolResponseBean freeTool;

	@Override
	public String toString() {
		return "ProductionToolResponseBean [id=" + id + ", uuid=" + uuid + ", toolName=" + toolName + ", toolRevision="
				+ toolRevision + ", toolIpAddress=" + toolIpAddress + ", toolOperator=" + toolOperator
				+ ", eventTimestamp=" + eventTimestamp + ", eventType=" + eventType + ", batTest=" + batTest
				+ ", spareTool=" + spareTool + ", tireTool=" + tireTool + ", lockTool=" + lockTool + ", freeTool="
				+ freeTool + "]";
	}


	
    
}
