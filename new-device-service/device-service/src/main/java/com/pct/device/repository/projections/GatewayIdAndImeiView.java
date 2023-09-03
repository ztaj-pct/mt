package com.pct.device.repository.projections;

public interface GatewayIdAndImeiView {

    Long getImei();

    void setImei(Long imei);

    Long getGatewayId();

    void setGatewayId(Long gatewayId);
}
