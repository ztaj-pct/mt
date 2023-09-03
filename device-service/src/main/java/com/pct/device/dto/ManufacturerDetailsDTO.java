package com.pct.device.dto;

import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
public class ManufacturerDetailsDTO {

	private String make;
	private String model;
	private String year;
}
