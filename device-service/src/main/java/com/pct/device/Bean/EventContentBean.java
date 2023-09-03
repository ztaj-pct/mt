package com.pct.device.Bean;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
@NoArgsConstructor
public class EventContentBean {
    private String tpmsMac;
    private String voltage;
    private String imei;
    private List<SpareToolSensorBean> spareToolSensorList;
    private String receiverType;
    private String receiverTypeDescription;
    private String trailerType;
    private String trailerTypeDescription;
    private String receiverSerialNumber;
    private String scanSheetVersion;
    private List<TireToolSensorBean> tireToolSensorList;
    private List<LockAndFreeToolListBean> lockList;

    @Override
	public String toString() {
		return "EventContentBean [tpmsMac=" + tpmsMac + ", voltage=" + voltage + ", imei=" + imei
				+ ", spareToolSensorList=" + spareToolSensorList + ", receiverType=" + receiverType
				+ ", receiverTypeDescription=" + receiverTypeDescription + ", trailerType=" + trailerType
				+ ", trailerTypeDescription=" + trailerTypeDescription + ", receiverSerialNumber="
				+ receiverSerialNumber + ", scanSheetVersion=" + scanSheetVersion + ", tireToolSensorList="
				+ tireToolSensorList + ", lockList=" + lockList + "]";
	}
	
    
    
    
}
