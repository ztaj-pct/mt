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
public class SensorBean {
    private String sensorUuid;
    private String status;
    private String productCode;
    private String gatewayUuid;
    private String can;
    private String macAddress;
    private Instant datetimeCreated;
    private Instant datetimeUpdated;
    private String displayName;
}
