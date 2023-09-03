package com.pct.device.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@Data
public class QueuedAtResponse {
	  private String last_request_epoch;
	  private String last_command_sent;
	  private String has_pending_command;
	  private String last_command_executed;
	  private String last_response_epoch;
	  private List<GatewayCommandResponse> gateway_command;
}
