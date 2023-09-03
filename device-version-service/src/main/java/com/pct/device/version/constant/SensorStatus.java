package com.pct.device.version.constant;

import java.util.HashMap;
import java.util.Map;

public enum SensorStatus {

	INITIALIZING("INITIALIZING"),  OFFLINE("Offline"), COMMUNICATION_ERROR("Communication Error"),
	HARDWARE_ERROR("Hardware Error"), ONLINE("Online"), APPLICATION_FAULT("Application Fault"),
	NOT_INSTALLED("Not Installed");

	public final Map<Integer, String> C_STATUSES = new HashMap<Integer, String>() {
		{
			put(0, "INITIALIZING");
			put(4, "Offline");
			put(8, "Communication Error");
			put(9, "Hardware Error");
			put(12, "Online");
			put(14, "Application Fault");
			put(15, "Not Installed");
		}
	};
	
    private final String value;

    SensorStatus(String value) {
        this.value = value;
    }

    public static final SensorStatus getCampaignStatus(String value) {
        if (value.equalsIgnoreCase(INITIALIZING.getValue())) {
            return SensorStatus.INITIALIZING;
        } else if (value.equalsIgnoreCase(OFFLINE.getValue())) {
            return SensorStatus.OFFLINE;
        } else if (value.equalsIgnoreCase(COMMUNICATION_ERROR.getValue())) {
            return SensorStatus.COMMUNICATION_ERROR;
        } else if (value.equalsIgnoreCase(HARDWARE_ERROR.getValue())) {
            return SensorStatus.HARDWARE_ERROR;
        }  else if (value.equalsIgnoreCase(ONLINE.getValue())) {
            return SensorStatus.ONLINE;
        }else if (value.equalsIgnoreCase(APPLICATION_FAULT.getValue())) {
            return SensorStatus.APPLICATION_FAULT;
        }else if (value.equalsIgnoreCase(NOT_INSTALLED.getValue())) {
            return SensorStatus.NOT_INSTALLED;
        }
        return null;
    }

    public String getValue() {
        return this.value;
    }
}
