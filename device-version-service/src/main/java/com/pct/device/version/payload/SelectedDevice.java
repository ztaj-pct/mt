package com.pct.device.version.payload;

import java.util.List;

public class SelectedDevice {
    private List<DeviceWithEligibility> deviceWithEligibilityList;
    private List<String> invalidImeiList;

    public SelectedDevice() {
    }

    public List<DeviceWithEligibility> getDeviceWithEligibilityList() {
        return deviceWithEligibilityList;
    }

    public void setDeviceWithEligibilityList(List<DeviceWithEligibility> deviceWithEligibilityList) {
        this.deviceWithEligibilityList = deviceWithEligibilityList;
    }

    public List<String> getInvalidImeiList() {
        return invalidImeiList;
    }

    public void setInvalidImeiList(List<String> invalidImeiList) {
        this.invalidImeiList = invalidImeiList;
    }
}