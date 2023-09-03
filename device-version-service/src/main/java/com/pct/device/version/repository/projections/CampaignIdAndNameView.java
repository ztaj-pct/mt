package com.pct.device.version.repository.projections;

import java.io.Serializable;

public interface CampaignIdAndNameView extends Serializable {

    String getCampaign_name();
    String getUuid();
}
