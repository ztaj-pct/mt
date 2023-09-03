package com.pct.common.payload;

import lombok.Data;

/**
 * @author Abhishek on 20/04/20
 */

@Data
public class JobSummaryRequest {

    private String accountNumber;
    private String location;
}
