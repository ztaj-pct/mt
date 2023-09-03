package com.pct.device.version.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DeviceCampaignHistory {

	private String campaingName;
	private String lastStep;
	private String lastStepTime;
	private String campaingStatus;
	private String lastStepStatus;
	private String BASEBAND_SW_VERSION;
	private String APP_SW_VERSION;
	private String 	BLE_Version;
	public String config1CRC;
	public String config1CIV;

	public String config2CRC;
	public String config2CIV;

	public String config3CRC;
	public String config3CIV;

	public String config4CRC;
	public String config4CIV;
}
