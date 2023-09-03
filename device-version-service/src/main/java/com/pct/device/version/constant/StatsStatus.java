package com.pct.device.version.constant;

/**
 * @author Dhruv 
 */
public enum StatsStatus {

	COMPLETED("Completed"), ON_HOLD("On hold"), PENDING("Pending"), IN_PROGRESS("In progress"), REMOVED("Removed"), OFF_HOLD("OFF hold");

    private final String value;

    StatsStatus(String value) {
        this.value = value;
    }

    public static final StatsStatus getCampaignStatus(String value) {
        if (value.equalsIgnoreCase(COMPLETED.getValue())) {
            return StatsStatus.COMPLETED;
        } else if (value.equalsIgnoreCase(ON_HOLD.getValue())) {
            return StatsStatus.ON_HOLD;
        } else if (value.equalsIgnoreCase(PENDING.getValue())) {
            return StatsStatus.PENDING;
        } else if (value.equalsIgnoreCase(IN_PROGRESS.getValue())) {
            return StatsStatus.IN_PROGRESS;
        }else if (value.equalsIgnoreCase(REMOVED.getValue())) {
            return StatsStatus.REMOVED;
        }
        else if (value.equalsIgnoreCase(OFF_HOLD.getValue())) {
            return StatsStatus.OFF_HOLD;
        }
        return null;
    }

    public String getValue() {
        return this.value;
    }
}
