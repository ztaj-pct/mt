package com.pct.device.dto;

import com.pct.common.model.Company;

public class DeviceSummaryDTO {

    private String name;
    private Integer devicesRegistered;
    private Integer devicesActiveWithGeotab;

    public DeviceSummaryDTO(String name, Integer devicesRegistered, Integer devicesActiveWithGeotab) {
        super();
        this.name = name;
        this.devicesRegistered = devicesRegistered;
        this.devicesActiveWithGeotab = devicesActiveWithGeotab;
    }

    public DeviceSummaryDTO(Company fleet) {
        this.name = fleet.getCompanyName();
        this.devicesRegistered = null;
        this.devicesActiveWithGeotab = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDevicesRegistered() {
        return devicesRegistered;
    }

    public void setDevicesRegistered(Integer devicesRegistered) {
        this.devicesRegistered = devicesRegistered;
    }

    public Integer getDevicesActiveWithGeotab() {
        return devicesActiveWithGeotab;
    }

    public void setDevicesActiveWithGeotab(Integer devicesActiveWithGeotab) {
        this.devicesActiveWithGeotab = devicesActiveWithGeotab;
    }

}
