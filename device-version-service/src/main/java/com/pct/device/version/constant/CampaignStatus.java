package com.pct.device.version.constant;

import com.pct.device.version.util.Constants;

/**
 * @author Dhruv 
 */
public enum CampaignStatus {

    NOT_STARTED("Not Started"), PAUSED("Paused"), IN_PROGRESS("In progress"),
    FINISHED("Finished"), PROBLEM("Problem"), STOPPED("Stopped"), ARCHIVED("Archived"), RETIRED("Retired");

    private final String value;

    CampaignStatus(String value) {
        this.value = value;
    }

    public static final CampaignStatus getCampaignStatus(String value) {
        if (value.equalsIgnoreCase(NOT_STARTED.getValue())) {
            return CampaignStatus.NOT_STARTED;
        } else if (value.equalsIgnoreCase(PAUSED.getValue())) {
            return CampaignStatus.PAUSED;
        } else if (value.equalsIgnoreCase(IN_PROGRESS.getValue())) {
            return CampaignStatus.IN_PROGRESS;
        } else if (value.equalsIgnoreCase(FINISHED.getValue())) {
            return CampaignStatus.FINISHED;
        } else if (value.equalsIgnoreCase(PROBLEM.getValue())) {
            return CampaignStatus.PROBLEM;
       } else if (value.equalsIgnoreCase(STOPPED.getValue())) {
            return CampaignStatus.STOPPED;
       } else if (value.equalsIgnoreCase(ARCHIVED.getValue())) {
           return CampaignStatus.ARCHIVED;
       } else if (value.equalsIgnoreCase(RETIRED.getValue())) {
           return CampaignStatus.RETIRED;
       }
        return null;
    }

    public String getValue() {
        return this.value;
    }
    
    
    public static final CampaignStatus getCampaignStatusInSearch(String value) {
        if (NOT_STARTED.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
            return CampaignStatus.NOT_STARTED;
        } else if (PAUSED.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
            return CampaignStatus.PAUSED;
        } else if (IN_PROGRESS.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
            return CampaignStatus.IN_PROGRESS;
        } else if (FINISHED.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
            return CampaignStatus.FINISHED;
        } else if (PROBLEM.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
            return CampaignStatus.PROBLEM;
       } else if (STOPPED.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
            return CampaignStatus.STOPPED;
       } else if (ARCHIVED.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
           return CampaignStatus.ARCHIVED;
       } else if (RETIRED.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
          return CampaignStatus.RETIRED;
      }/* else if (value.trim().toLowerCase().matches(".*\\d.*") || Constants.COMPLETION_PATTERN.trim().toLowerCase().contains(value)) {
           return CampaignStatus.IN_PROGRESS;
      }*/
        return null;
    }

    
}
