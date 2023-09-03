package com.pct.device.version.repository.projections;

import java.sql.Timestamp;

public interface CampaignStepView {

    String getStepStatus();

    void setStepStatus(String status);

    Timestamp getStepTime();

    void setStepTime(Timestamp time);
}
