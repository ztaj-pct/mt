package com.pct.device.payload;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.device.Bean.SensorBean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
@NoArgsConstructor
public class GatewayBeanForMobileApp {
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
    private String action;
}