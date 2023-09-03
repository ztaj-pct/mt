package com.pct.device.version.repository.projections;

import java.io.Serializable;

public interface CampaignPauseView extends Serializable {

    Long getPause_limit();
    Boolean getPause_execution();
//    void setTarget_value(String campaignUuid);
}
