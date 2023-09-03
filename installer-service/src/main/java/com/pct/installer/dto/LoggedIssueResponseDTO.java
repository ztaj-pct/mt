package com.pct.installer.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class LoggedIssueResponseDTO {

	private String date;
	private String status;
	private String sensor;
	private String reasonCode;
	private String comment;
	private String user;
}
