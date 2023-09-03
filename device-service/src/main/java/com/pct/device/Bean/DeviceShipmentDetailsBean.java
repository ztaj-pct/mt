package com.pct.device.Bean;

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
public class DeviceShipmentDetailsBean {

    private Long id;

    private Long deviceId;

    private String accountNumber;

    private Instant dateShipped;

    private String addressShipped;
}
