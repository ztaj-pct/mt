package com.pct.device.version.repository.projections;

import java.io.Serializable;

public interface DeviceCampaignStepStatus extends Serializable {

    String getDevice_id();
    String getStatus();
    Long getStep_order_number();
//    void setTarget_value(String campaignUuid);
}
