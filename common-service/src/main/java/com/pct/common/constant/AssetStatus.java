package com.pct.common.constant;

/**
 * @author Abhishek on 16/04/20
 */
public enum AssetStatus {

    PARTIAL("Partial"), PENDING("Pending"), INSTALL_IN_PROGRESS("Install in progress"), ACTIVE("Active"), INACTIVE("Inactive"), DELETED("Deleted"), ERROR("Error");

    private final String value;

    AssetStatus(String value) {
        this.value = value;
    }

    public final static AssetStatus getAssetStatus(String status) {

        if (status.equalsIgnoreCase(PENDING.getValue())) {
            return PENDING;
        } else if (status.equalsIgnoreCase(ACTIVE.getValue())) {
            return ACTIVE;
        }else if (status.equalsIgnoreCase(INSTALL_IN_PROGRESS.getValue())) {
                return INSTALL_IN_PROGRESS;
        } else if (status.equalsIgnoreCase(INACTIVE.getValue())) {
            return INACTIVE;
        } else if (status.equalsIgnoreCase(DELETED.getValue())) {
            return DELETED;
        } else if (status.equalsIgnoreCase(ERROR.getValue())) {
            return ERROR;
        }
        return null;
    }

    public String getValue() {
        return this.value;
    }
}
