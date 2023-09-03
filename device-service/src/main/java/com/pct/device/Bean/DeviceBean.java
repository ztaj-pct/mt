package com.pct.device.Bean;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pct.common.model.Device;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
@NoArgsConstructor
public class DeviceBean {
    private String deviceUuid;
    private String imei;
    private String can;
    private String productCode;
    private String productName;
    private String status;
    private String type;
    private String macAddress;
    private Instant dateCreated;
    private Instant dateUpdated;
    private List<Device> deviceList;
    private String action;
    private String displayName;
}
