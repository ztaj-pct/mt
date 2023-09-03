package com.pct.common.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductMasterDto {
	
	private String uuid;
	private String productCode;
	private String productName;
	
	public ProductMasterDto() {
		super();
	}
	
	public ProductMasterDto(String uuid, String productCode, String productName) {
		super();
		this.uuid = uuid;
		this.productCode = productCode;
		this.productName = productName;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getProductCode() {
		return productCode;
	}
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	
	

}
