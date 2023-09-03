package com.pct.device.version.constant;

import com.pct.device.version.util.Constants;

/**
 * @author Dhruv 
 */
public enum CampaignListDisplayStatus {

    PENDING("Pending");
    private final String value;

    CampaignListDisplayStatus(String value) {
        this.value = value;
    }

    public static final CampaignListDisplayStatus getCampaignStatus(String value) {
        if (value.equalsIgnoreCase(PENDING.getValue())) {
            return CampaignListDisplayStatus.PENDING;
        } 
        return null;
    }

    public String getValue() {
        return this.value;
    }
    
    

    
}
