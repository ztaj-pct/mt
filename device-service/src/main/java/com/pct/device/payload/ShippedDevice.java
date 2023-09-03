package com.pct.device.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Abhishek on 01/07/20
 */

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@NoArgsConstructor
public class ShippedDevice {

    private String epicorOrderId;
    private String salesforceOrderNumber;
    private String salesforceAccountId;
    private String packingSlipNumber;
    private String productCode;
    private int quantityShipped;
    private String orderItemNumber;
    private String shipmentDetails;
    private List<String> imei;
}
