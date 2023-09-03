package com.pct.common.constant;

/**
 * @author Abhishek on 16/04/20
 */
public enum SensorStatus {

    PENDING("Pending"), INSTALLED("Installed"), ERROR("Error"), PROBLEM("Problem");

    private final String value;

    SensorStatus(String value) {
        this.value = value;
    }

    public static final SensorStatus getSensorStatus(String value) {
        if (value.equalsIgnoreCase(PENDING.getValue())) {
            return SensorStatus.PENDING;
        } else if (value.equalsIgnoreCase(INSTALLED.getValue())) {
            return SensorStatus.INSTALLED;
        } else if (value.equalsIgnoreCase(ERROR.getValue())) {
            return SensorStatus.ERROR;
        } else if (value.equalsIgnoreCase(PROBLEM.getValue())) {
            return SensorStatus.PROBLEM;
        }
        return null;
    }

    public String getValue() {
        return this.value;
    }
}
