package com.pct.es.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data

public class BetaAbsId {
    public int id;
    public String status;
    public String receivedTimeStamp;
    public int selector;
    public String name;
    public String displayName;
    public Object manufacturerInfo;
    public int serialNum;
    public int absFwVersion;
    public int model;
}
