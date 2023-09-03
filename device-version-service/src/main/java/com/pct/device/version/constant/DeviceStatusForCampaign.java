package com.pct.device.version.constant;

/**
 * @author Dhruv 
 */
public enum DeviceStatusForCampaign {

	ELIGIBLE("Eligible"), NOT_ELIGIBLE("Not Eligible"),OFF_PATH("Off Path"),
	ON_PATH("On Path"),
	UNKNOWN("Unknown"), COMPLETED("Completed"), PROBLEM("Problem"),PENDNG("Pending"),NOTSTARTED("Not Started"),SUCCESS("Success"),EXCLUDED("Excluded"),REMOVED("Removed");

    private final String value;

    DeviceStatusForCampaign(String value) {
        this.value = value;
    }

    public static final DeviceStatusForCampaign getCampaignStatus(String value) {
        if (value.equalsIgnoreCase(ELIGIBLE.getValue())) {
            return DeviceStatusForCampaign.ELIGIBLE;
        } else if (value.equalsIgnoreCase(NOT_ELIGIBLE.getValue())) {
            return DeviceStatusForCampaign.NOT_ELIGIBLE;
        } else if (value.equalsIgnoreCase(UNKNOWN.getValue())) {
            return DeviceStatusForCampaign.UNKNOWN;
        } else if (value.equalsIgnoreCase(COMPLETED.getValue())) {
            return DeviceStatusForCampaign.COMPLETED;
        }  else if (value.equalsIgnoreCase(PROBLEM.getValue())) {
            return DeviceStatusForCampaign.PROBLEM;
        }else if (value.equalsIgnoreCase(OFF_PATH.getValue())) {
            return DeviceStatusForCampaign.OFF_PATH;
        }else if (value.equalsIgnoreCase(ON_PATH.getValue())) {
            return DeviceStatusForCampaign.ON_PATH;
        }
        else if (value.equalsIgnoreCase(PENDNG.getValue())) {
            return DeviceStatusForCampaign.PENDNG;
        }
        else if (value.equalsIgnoreCase(NOTSTARTED.getValue())) {
            return DeviceStatusForCampaign.NOTSTARTED;
        }
        else if (value.equalsIgnoreCase(SUCCESS.getValue())) {
            return DeviceStatusForCampaign.SUCCESS;
        }
        else if (value.equalsIgnoreCase(EXCLUDED.getValue())) {
            return DeviceStatusForCampaign.EXCLUDED;
        }
        else if (value.equalsIgnoreCase(REMOVED.getValue())) {
            return DeviceStatusForCampaign.REMOVED;
        }
        return null;
    }

    public String getValue() {
        return this.value;
    }
}
