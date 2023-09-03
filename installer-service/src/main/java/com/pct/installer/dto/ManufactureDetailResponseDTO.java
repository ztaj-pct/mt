package com.pct.installer.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ManufactureDetailResponseDTO {
	private String make;
    private String model;
    private String year;

}
