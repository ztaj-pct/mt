package com.pct.device.version.repository.projections;

import java.io.Serializable;

public interface StepStatusForDeviceView extends Serializable {

    String getDevice_id();
    String getStatus();
//    void setTarget_value(String campaignUuid);
}
