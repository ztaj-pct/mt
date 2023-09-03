package com.pct.es.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data

public class ConfigVersion {
    public int id;
    public Object status;
    public String receivedTimeStamp;
    public Object selector;
    public String name;
    public String displayName;
    public int deviceConfig;
    public String configurationDesc;
}
