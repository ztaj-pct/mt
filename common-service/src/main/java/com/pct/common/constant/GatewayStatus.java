package com.pct.common.constant;

public enum GatewayStatus {

	PENDING("Pending"), INSTALL_IN_PROGRESS("Install in progress"), INSTALLED("Installed"),

	ACTIVE("Active"), INACTIVE("Inactive"),

	DELETED("Deleted"), ERROR("Error"), PROBLEM("Problem");

	private final String value;

	GatewayStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public static final GatewayStatus getGatewayStatusInSearch(String value) {
		if (PENDING.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
			return GatewayStatus.PENDING;
		} else if (INSTALL_IN_PROGRESS.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
			return GatewayStatus.INSTALL_IN_PROGRESS;
		} else if (INSTALLED.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
			return GatewayStatus.INSTALLED;
		} else if (ACTIVE.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
			return GatewayStatus.ACTIVE;
		} else if (PROBLEM.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
			return GatewayStatus.PROBLEM;
		} else if (INACTIVE.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
			return GatewayStatus.INACTIVE;
		} else if (DELETED.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
			return GatewayStatus.DELETED;
		} else if (ERROR.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
			return GatewayStatus.ERROR;
		}
		return null;
	}
}
