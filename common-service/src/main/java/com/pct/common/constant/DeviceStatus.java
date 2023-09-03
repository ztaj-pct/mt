package com.pct.common.constant;

/**
 * @author Abhishek on 16/04/20
 */
public enum DeviceStatus {

    PENDING("Pending"), INSTALL_IN_PROGRESS("Install in progress"), INSTALLED("Installed"),

    ACTIVE("Active"), INACTIVE("Inactive"),

    DELETED("Deleted"), ERROR("Error"), PROBLEM("Problem"),
	
	ACTIVE_NOT_IA("Active not IA");

    private final String value;

    DeviceStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
    
	public static final DeviceStatus getGatewayStatusInSearch(String value) {
		if (PENDING.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
			return DeviceStatus.PENDING;
		} else if (INSTALL_IN_PROGRESS.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
			return DeviceStatus.INSTALL_IN_PROGRESS;
		} else if (INSTALLED.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
			return DeviceStatus.INSTALLED;
		} else if (ACTIVE.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
			return DeviceStatus.ACTIVE;
		} else if (PROBLEM.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
			return DeviceStatus.PROBLEM;
		} else if (INACTIVE.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
			return DeviceStatus.INACTIVE;
		} else if (DELETED.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
			return DeviceStatus.DELETED;
		} else if (ERROR.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
			return DeviceStatus.ERROR;
		}else if (ACTIVE_NOT_IA.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
			return DeviceStatus.ACTIVE_NOT_IA;
		}
		return null;
	}
}
