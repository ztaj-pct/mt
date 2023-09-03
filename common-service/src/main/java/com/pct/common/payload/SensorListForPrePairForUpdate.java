package com.pct.common.payload;

import java.util.List;

import javax.persistence.Column;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SensorListForPrePairForUpdate {
	
	@Column(name = "uuid")
    private String uuid;

	@JsonProperty("product_code")
	private String productCode;
	
	@JsonProperty("product_name")
	private String productName;
	
	@JsonProperty("mac_address")
    private String macAddress;
	
	@JsonProperty("sub_sensor_list")
    private List<SubSensorListForUpdate> subSensorList;
	
}
