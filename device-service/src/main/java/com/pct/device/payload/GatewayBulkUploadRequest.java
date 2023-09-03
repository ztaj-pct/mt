package com.pct.device.payload;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class GatewayBulkUploadRequest {
	
	private CompanyPayload company;
	private Boolean isAllImei;
	private Boolean isAllMacAddress;
	private Boolean status;
	private ProductMasterResponse productMasterResponse;
	private String productCode;
	private String salesforceOrderId;
	private String usageStatus;
	private String telecomProvider;
	private List<Map<String,Object>> gatewayList;

}
