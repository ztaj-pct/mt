package com.pct.device.version.constant;

import com.pct.device.version.util.Constants;

/**
 * @author Dhruv 
 */
public enum CampaignInitialisingStatus {

    NOT_STARTED("Not Started"), PAUSED("Paused"), IN_PROGRESS("In progress"),
    FINISHED("Finished"), PROBLEM("Problem"), STOPPED("Stopped"), ARCHIVED("Archived"), RETIRED("Retired");

    private final String value;

    CampaignInitialisingStatus(String value) {
        this.value = value;
    }

    public static final CampaignInitialisingStatus getCampaignStatus(String value) {
        if (value.equalsIgnoreCase(NOT_STARTED.getValue())) {
            return CampaignInitialisingStatus.NOT_STARTED;
        } else if (value.equalsIgnoreCase(PAUSED.getValue())) {
            return CampaignInitialisingStatus.PAUSED;
        } else if (value.equalsIgnoreCase(IN_PROGRESS.getValue())) {
            return CampaignInitialisingStatus.IN_PROGRESS;
        } else if (value.equalsIgnoreCase(FINISHED.getValue())) {
            return CampaignInitialisingStatus.FINISHED;
        } else if (value.equalsIgnoreCase(PROBLEM.getValue())) {
            return CampaignInitialisingStatus.PROBLEM;
       } else if (value.equalsIgnoreCase(STOPPED.getValue())) {
            return CampaignInitialisingStatus.STOPPED;
       } else if (value.equalsIgnoreCase(ARCHIVED.getValue())) {
           return CampaignInitialisingStatus.ARCHIVED;
       } else if (value.equalsIgnoreCase(RETIRED.getValue())) {
           return CampaignInitialisingStatus.RETIRED;
       }
        return null;
    }

    public String getValue() {
        return this.value;
    }
    
    
    public static final CampaignInitialisingStatus getCampaignStatusInSearch(String value) {
        if (NOT_STARTED.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
            return CampaignInitialisingStatus.NOT_STARTED;
        } else if (PAUSED.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
            return CampaignInitialisingStatus.PAUSED;
        } else if (IN_PROGRESS.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
            return CampaignInitialisingStatus.IN_PROGRESS;
        } else if (FINISHED.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
            return CampaignInitialisingStatus.FINISHED;
        } else if (PROBLEM.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
            return CampaignInitialisingStatus.PROBLEM;
       } else if (STOPPED.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
            return CampaignInitialisingStatus.STOPPED;
       } else if (ARCHIVED.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
           return CampaignInitialisingStatus.ARCHIVED;
       } else if (RETIRED.getValue().trim().toLowerCase().contains(value.trim().toLowerCase())) {
          return CampaignInitialisingStatus.RETIRED;
      }/* else if (value.trim().toLowerCase().matches(".*\\d.*") || Constants.COMPLETION_PATTERN.trim().toLowerCase().contains(value)) {
           return CampaignStatus.IN_PROGRESS;
      }*/
        return null;
    }

    
}
