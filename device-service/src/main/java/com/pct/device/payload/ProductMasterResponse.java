package com.pct.device.payload;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductMasterResponse {

	private Long id;
	private String uuid;
	private String product_code;
    private String product_name;
    private String type;
    private String subtype;
}
