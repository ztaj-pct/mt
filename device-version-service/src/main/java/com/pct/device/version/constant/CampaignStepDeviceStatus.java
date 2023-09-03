package com.pct.device.version.constant;

/**
 * @author Dhruv 
 */
public enum CampaignStepDeviceStatus {

	SUCCESS("Success"), FAILED("Failed"), PENDING("Pending"), PROBLEM("Problem"), REMOVED("Removed"),NOTSTARTED("Not Started"),EXCLUDED("Excluded");

    private final String value;

    CampaignStepDeviceStatus(String value) {
        this.value = value;
    }

    public static final CampaignStepDeviceStatus getCampaignStatus(String value) {
        if (value.equalsIgnoreCase(SUCCESS.getValue())) {
            return CampaignStepDeviceStatus.SUCCESS;
        } else if (value.equalsIgnoreCase(FAILED.getValue())) {
            return CampaignStepDeviceStatus.FAILED;
        } else if (value.equalsIgnoreCase(PENDING.getValue())) {
            return CampaignStepDeviceStatus.PENDING;
        } else if (value.equalsIgnoreCase(PROBLEM.getValue())) {
            return CampaignStepDeviceStatus.PROBLEM;
        }else if (value.equalsIgnoreCase(REMOVED.getValue())) {
            return CampaignStepDeviceStatus.REMOVED;
        }
        else if (value.equalsIgnoreCase(NOTSTARTED.getValue())) {
            return CampaignStepDeviceStatus.NOTSTARTED;
        }
        else if (value.equalsIgnoreCase(EXCLUDED.getValue())) {
            return CampaignStepDeviceStatus.EXCLUDED;
        }
        else if (value.equalsIgnoreCase(REMOVED.getValue())) {
            return CampaignStepDeviceStatus.REMOVED;
        }
        
        return null;
    }

    public String getValue() {
        return this.value;
    }
}
