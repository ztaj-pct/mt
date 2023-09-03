package com.pct.device.service.device;

import java.io.Serializable;

public interface DeviceReportView extends Serializable {
    String                  getDEVICE_ID();
    String                  getBASEBAND_SW_VERSION();
    String                  getAPP_SW_VERSION();
    String                  getEXTENDER_VERSION();
    
}
