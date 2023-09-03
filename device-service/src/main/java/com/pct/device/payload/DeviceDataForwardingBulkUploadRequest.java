package com.pct.device.payload;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.payload.Forwarding;

import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class DeviceDataForwardingBulkUploadRequest {
	
	private List<String> imeis;
	
	@JsonProperty("end_customer_account_number")
	private String endCustomerAccountNumber;
	
	@JsonProperty("purchase_by_account_number")
	private String purchasedByAccountNumber;
	
	@JsonProperty("forwarding_rules")
	private List<Forwarding> forwardingRules;
	
	@JsonProperty("ignore_forwarding_rules")
	private List<Forwarding> ignoreForwardingRules;

	private String action;
}
