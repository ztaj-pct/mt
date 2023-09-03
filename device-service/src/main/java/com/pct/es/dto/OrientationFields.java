package com.pct.es.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data

public class OrientationFields {
    public int id;
    public Object status;
    public String receivedTimeStamp;
    public Object selector;
    public String name;
    public String displayName;
    public int orientationStatusIndex;
    public int orientationXaxisMilliGs;
    public int orientationYaxisMilliGs;
    public int orientationZaxisMilliGs;
}
