package com.pct.es.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data

public class Waterfall {
    public int id;
    public Object status;
    public String receivedTimeStamp;
    public int selector;
    public String name;
    public String displayName;
    public int numberOfFiles;
    public List<WaterfallInfo> waterfallInfo;
}
