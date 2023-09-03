package com.pct.common.constant;

/**
 * @author Abhishek on 22/04/20
 */
public enum CommunicationStatus {

    NA("NA");

    private final String value;

    CommunicationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
