package com.pct.common.dto;

import java.time.Instant;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
public class DeviceCommandResponseDTO {


    private String uuid;
	private String deviceId;
	private String atCommand;
    private String deviceResponse;
    private String status;

    public DeviceCommandResponseDTO(String uuid, String deviceId, String atCommand, String deviceResponse,
			String status) {
		this.uuid = uuid;
		this.deviceId = deviceId;
		this.atCommand = atCommand;
		this.deviceResponse = deviceResponse;
		this.status = status;
	}


	public DeviceCommandResponseDTO() {
		
	}


    
    
}
