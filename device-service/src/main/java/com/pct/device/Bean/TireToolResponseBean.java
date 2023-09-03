package com.pct.device.Bean;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class TireToolResponseBean {
	
	private Long id;
	
	private String uuid;
	
	private String receiverType;
	
	private String receiverTypeDescription;
	
	private String trailerType;
	
	private String trailerTypeDescription;
	
	private String receiverSerialNumber;
	
	private String scanSheetVersion;
	
	private List<TireToolSensorResponseBean> tireToolSensors;

	@Override
	public String toString() {
		return "TireTool [id=" + id + ", uuid=" + uuid + ", receiverType=" + receiverType + ", receiverTypeDescription="
				+ receiverTypeDescription + ", trailerType=" + trailerType + ", trailerTypeDescription="
				+ trailerTypeDescription + ", receiverSerialNumber=" + receiverSerialNumber + ", scanSheetVersion="
				+ scanSheetVersion + ", tireToolSensors=" + tireToolSensors + "]";
	}
}
