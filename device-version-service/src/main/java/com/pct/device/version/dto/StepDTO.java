package com.pct.device.version.dto;

import java.math.BigInteger;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.ToString;


@Data

@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class StepDTO {
	public StepDTO(String packageName, BigInteger  count, String stepUuid, String status) {
		super();
		this.packageName = packageName;
		this.count = count;
		this.stepUuid = stepUuid;
		this.status = status;
	}
	private String packageName;
	private BigInteger  count;
	private String stepUuid;
	private String status;
}