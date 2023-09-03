package com.pct.device.dto;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class GatewayCommandResponse {
	
	 private Integer priority;
     private String uuid;
     private String created_epoch;
     private String at_command;
     private String created_by;

}
