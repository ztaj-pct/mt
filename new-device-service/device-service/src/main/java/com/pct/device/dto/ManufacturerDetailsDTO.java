package com.pct.device.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Abhishek on 05/06/20
 */

@Data
@NoArgsConstructor
public class ManufacturerDetailsDTO {

    private String make;
    private String model;
    private String year;
}
