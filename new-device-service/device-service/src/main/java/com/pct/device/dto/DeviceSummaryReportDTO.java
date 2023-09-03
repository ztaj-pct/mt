package com.pct.device.dto;

import java.util.List;


public class DeviceSummaryReportDTO {

    List<DeviceSummaryDTO> registeredDevice;

    DeviceSummaryDTO unRgistereredDevice;

    public List<DeviceSummaryDTO> getRegisteredDevice() {
        return registeredDevice;
    }

    public void setRegisteredDevice(List<DeviceSummaryDTO> registeredDevice) {
        this.registeredDevice = registeredDevice;
    }

    public DeviceSummaryDTO getUnRgistereredDevice() {
        return unRgistereredDevice;
    }

    public void setUnRgistereredDevice(DeviceSummaryDTO unRgistereredDevice) {
        this.unRgistereredDevice = unRgistereredDevice;
    }

}
