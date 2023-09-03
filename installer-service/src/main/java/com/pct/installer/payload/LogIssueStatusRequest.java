package com.pct.installer.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogIssueStatusRequest {
//	 @JsonProperty("logIssueUuid")
	private String logIssueUuid;
	private String status;
}
