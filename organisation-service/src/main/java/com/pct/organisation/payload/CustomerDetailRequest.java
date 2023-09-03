package com.pct.organisation.payload;

import lombok.Data;

/**
 * @author Abhishek on 24/04/20
 */

@Data
public class CustomerDetailRequest {

	private Long customerId;
    private String location;
}
