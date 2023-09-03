package com.pct.es.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
@Data

public class SoftwareVersion {
    public int id;
    public Object status;
    public String receivedTimeStamp;
    public int selector;
    public String name;
    public String displayName;
    public int hwIdVersion;
    public String osVersion;
    public String deviceName;
    public String appVersion;
    public String extenderVersion;
    public String bleVersion;
    public int hwVersionRevision;
}
