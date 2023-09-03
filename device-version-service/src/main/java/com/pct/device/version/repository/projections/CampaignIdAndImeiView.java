package com.pct.device.version.repository.projections;

import java.io.Serializable;

public interface CampaignIdAndImeiView extends Serializable {

    String getUuid();

//    void setUuid(String uuid);

    String getTarget_value();
    String getGrouping_name();
//    void setTarget_value(String campaignUuid);
}
