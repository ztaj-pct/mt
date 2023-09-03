package com.pct.common.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.constant.InstanceType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SubSensorList {

	@JsonProperty("instance_type")
	private String instanceType;

	@JsonProperty("sub_sensor_id")
	private String subSensorId;
	
	@JsonProperty("type")
	private String type;

}
