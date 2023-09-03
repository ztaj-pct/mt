package com.pct.device.bean;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
@NoArgsConstructor
public class GatewayShipmentDetailsBean {

    private Long id;

    private Long gatewayId;

    private String accountNumber;

    private Instant dateShipped;

    private String addressShipped;
}
