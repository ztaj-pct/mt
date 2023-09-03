package com.pct.device.payload;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pct.device.dto.SensorDataDTO;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Setter
@Getter
public class UpdateGatewayRequest {
	
	@JsonProperty("is_replace_existing_configuration")
    private boolean isReplaceExistingConfiguration;
	
	@JsonProperty("is_standard_configuration")
    private boolean isStandardConfiguration;
	
    @JsonProperty("imei_list")
    private List<String> imeiList;
    
    @JsonProperty("sensor_list")
    private List<SensorDataDTO> sensorList;
    
    @JsonProperty("section_id")
    private String sectionId;

}
