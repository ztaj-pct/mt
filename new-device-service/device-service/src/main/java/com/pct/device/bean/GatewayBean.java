package com.pct.device.bean;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
@NoArgsConstructor
public class GatewayBean {
    private String gatewayUuid;
    private String imei;
    private String can;
    private String productCode;
    private String productName;
    private String status;
    private String type;
    private String macAddress;
    private Instant dateCreated;
    private Instant dateUpdated;
    private List<SensorBean> sensorList;
}
