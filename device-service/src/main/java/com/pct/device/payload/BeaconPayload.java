package com.pct.device.payload;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.IOTType;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class BeaconPayload {
	
//	
//	   @JsonProperty("product_short_name")
//	    private String productShortName;
//
//	    @JsonProperty("product_code")
//	    private String productCode;
//
//	    @JsonProperty("quantity_shipped")
//	    private String quantityShipped;
//	   
//	    @JsonProperty("mac_Address")
//	    private String macAddress;
	    
	private String productCode;
	private String productName;
	private String appVersion;
	private String mcuVersion;
	private String binVersion;
	private String bleVersion;
	private String config1;
	private String epicorOrderNumber;
	private String can;
	private String son;
	private String macAddress;
	private String uuid;
    private String updatedBy;
    private String createdBy;
    private String quantityShipped;
    private DeviceStatus status;
    private IOTType type;
	

}
