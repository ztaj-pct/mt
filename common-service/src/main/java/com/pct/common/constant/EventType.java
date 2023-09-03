package com.pct.common.constant;

public enum EventType {

    INSTALLATION("Installation"), VERIFICATION("Verification"), ISSUE("Issue"),
    INSTALLATION_COMPLETE("Installation Complete"),
    GATEWAY_INSTALLATION_COMPLETE("Gateway Installation Complete"),
    SENSOR_INSTALLATION_COMPLETE("Sensor Installation Complete"),
    REJECTED_UPDATE("Rejected Update");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
